package com.example.demo.security;

import com.example.demo.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/** Создание и проверка access- и refresh-токенов (JWT, HS256). */
@Component
public class JwtService {

    public static final String TYPE_CLAIM = "type";
    public static final String ROLE_CLAIM = "role";
    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";
    private static final String ALGORITHM = "HS256";

    /** Выданный токен и его exp (с секундной точностью, как в самом JWT). */
    public record IssuedToken(String token, String jti, Instant expiresAt) {
    }

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final JwtParser accessParser;
    private final JwtParser refreshParser;

    public JwtService(JwtProperties props) {
        if (props.secret() == null || props.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) должен быть не короче 32 байт");
        }
        if (props.accessTtl() == null || props.refreshTtl() == null
                || props.accessTtl().toSeconds() < 1 || props.refreshTtl().toSeconds() < 1) {
            throw new IllegalStateException("jwt.access-ttl и jwt.refresh-ttl должны быть не меньше 1 секунды");
        }
        this.key = new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.accessTtl = props.accessTtl();
        this.refreshTtl = props.refreshTtl();
        this.accessParser = parser(ACCESS);
        this.refreshParser = parser(REFRESH);
    }

    public IssuedToken createAccessToken(User user) {
        return create(user, ACCESS, accessTtl);
    }

    public IssuedToken createRefreshToken(User user) {
        return create(user, REFRESH, refreshTtl);
    }

    public Claims validateAccessToken(String token) {
        return validate(token, accessParser);
    }

    public Claims validateRefreshToken(String token) {
        return validate(token, refreshParser);
    }

    private IssuedToken create(User user, String type, Duration ttl) {
        // JWT хранит iat/exp в секундах — отбрасываем доли, чтобы exp в БД совпадал с exp в токене.
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(ttl.toSeconds());
        String jti = UUID.randomUUID().toString();
        var builder = Jwts.builder()
                .id(jti)
                .subject(user.username)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(TYPE_CLAIM, type);
        if (ACCESS.equals(type)) {
            builder.claim(ROLE_CLAIM, user.role);
        }
        String token = builder.signWith(key, Jwts.SIG.HS256).compact();
        return new IssuedToken(token, jti, expiresAt);
    }

    private JwtParser parser(String type) {
        return Jwts.parser()
                .verifyWith(key)          // подпись; неподписанные токены (alg=none) отклоняются
                .require(TYPE_CLAIM, type) // тип токена
                .build();                 // срок действия (exp/nbf) проверяется парсером
    }

    private Claims validate(String token, JwtParser parser) {
        if (token == null || token.isBlank()) {
            throw new JwtValidationException("Токен не передан");
        }
        Jws<Claims> jws;
        try {
            jws = parser.parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtValidationException("Токен недействителен");
        }
        if (!ALGORITHM.equals(jws.getHeader().getAlgorithm())) {
            throw new JwtValidationException("Неподдерживаемый алгоритм подписи");
        }
        Claims claims = jws.getPayload();
        if (isBlank(claims.getSubject()) || isBlank(claims.getId())
                || claims.getIssuedAt() == null || claims.getExpiration() == null) {
            throw new JwtValidationException("В токене нет обязательных claims");
        }
        return claims;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
