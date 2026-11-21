package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.TokenPair;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final int MAX_DEVICE_ID = 255;

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository users;
    private final TokenService tokens;

    public AuthService(AuthenticationManager authenticationManager, UserService userService,
                       UserRepository users, TokenService tokens) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.users = users;
        this.tokens = tokens;
    }

    @Transactional
    public TokenPair login(LoginRequest req) {
        if (req == null || isBlank(req.username()) || isBlank(req.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите логин и пароль");
        }
        String deviceId = deviceId(req.deviceId());
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password()));
        } catch (AuthenticationException e) {
            // Одинаковый ответ для неверного пароля, несуществующего и заблокированного пользователя.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }
        User user = users.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));
        return tokens.issue(user, deviceId);
    }

    /** Пользователь и его первая сессия создаются в одной транзакции: ошибка записи сессии откатывает регистрацию. */
    @Transactional
    public TokenPair register(RegisterRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пустое тело запроса");
        }
        String deviceId = deviceId(req.deviceId());
        User u = new User();
        u.name = req.name();
        u.username = req.username();
        u.password = req.password();
        User saved = userService.register(u); // проверки данных, уникальности логина, роль всегда CUSTOMER
        return tokens.issue(saved, deviceId);
    }

    /**
     * Без @Transactional: транзакция открывается в TokenService.refresh, иначе внешняя транзакция
     * откатила бы перевод сессии в REVOKED при отказе.
     */
    public TokenPair refresh(RefreshRequest req) {
        if (req == null || isBlank(req.refreshToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите refreshToken");
        }
        return tokens.refresh(req.refreshToken());
    }

    private static String deviceId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String v = raw.trim();
        if (v.length() > MAX_DEVICE_ID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceId длиннее " + MAX_DEVICE_ID + " символов");
        }
        return v;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
