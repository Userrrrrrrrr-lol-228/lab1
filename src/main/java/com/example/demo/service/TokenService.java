package com.example.demo.service;

import com.example.demo.dto.TokenPair;
import com.example.demo.model.SessionStatus;
import com.example.demo.model.User;
import com.example.demo.model.UserSession;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSessionRepository;
import com.example.demo.security.JwtService;
import com.example.demo.security.JwtService.IssuedToken;
import com.example.demo.security.JwtValidationException;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Выдача и обновление пар токенов с учётом refresh-сессий в БД. */
@Service
public class TokenService {

    private final JwtService jwt;
    private final UserSessionRepository sessions;
    private final UserRepository users;

    public TokenService(JwtService jwt, UserSessionRepository sessions, UserRepository users) {
        this.jwt = jwt;
        this.sessions = sessions;
        this.users = users;
    }

    /** Новая пара + новая сессия ACTIVE. Пара возвращается только после записи сессии в БД. */
    @Transactional
    public TokenPair issue(User user, String deviceId) {
        IssuedToken access = jwt.createAccessToken(user);
        IssuedToken refresh = jwt.createRefreshToken(user);

        UserSession session = new UserSession();
        session.username = user.username;
        session.deviceId = deviceId;
        session.accessToken = access.token();
        session.refreshToken = refresh.token();
        session.accessTokenExpiry = access.expiresAt();
        session.refreshTokenExpiry = refresh.expiresAt();
        session.status = SessionStatus.ACTIVE;
        sessions.saveAndFlush(session);

        return new TokenPair(access.token(), refresh.token());
    }

    /**
     * Обмен refresh на новую пару в одной транзакции. Строка сессии блокируется (FOR UPDATE),
     * поэтому из двух параллельных обменов одного refresh успешен только один.
     * Отказ при повторном использовании (RefreshTokenReuseException) не откатывает перевод в REVOKED;
     * любая другая ошибка, в том числе техническая при сохранении новой пары, откатывает всё.
     */
    @Transactional(noRollbackFor = RefreshTokenReuseException.class)
    public TokenPair refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwt.validateRefreshToken(refreshToken);
        } catch (JwtValidationException e) {
            throw new InvalidRefreshTokenException("Недействительный refresh-токен");
        }

        UserSession session = sessions.findByRefreshTokenForUpdate(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Сессия не найдена"));
        if (!session.username.equals(claims.getSubject())) {
            throw new InvalidRefreshTokenException("Refresh-токен не принадлежит владельцу сессии");
        }

        Instant now = Instant.now();
        boolean notExpired = session.refreshTokenExpiry.isAfter(now);
        switch (session.status) {
            case REVOKED -> throw new InvalidRefreshTokenException("Сессия отозвана");
            case USED -> {
                if (notExpired) {
                    session.status = SessionStatus.REVOKED;
                    sessions.saveAndFlush(session);
                    throw new RefreshTokenReuseException();
                }
                throw new InvalidRefreshTokenException("Refresh-токен уже использован");
            }
            case ACTIVE -> {
                if (!notExpired) {
                    throw new InvalidRefreshTokenException("Срок действия refresh-токена истёк");
                }
            }
        }

        User user = users.findByUsername(session.username)
                .filter(u -> Boolean.TRUE.equals(u.enabled))
                .orElseThrow(() -> new InvalidRefreshTokenException("Пользователь не найден или заблокирован"));

        session.status = SessionStatus.USED;
        sessions.save(session);
        return issue(user, session.deviceId);
    }
}
