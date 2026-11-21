package com.example.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static com.example.demo.HttpSupport.json;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowTests extends PostgresTestBase {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate db;

    HttpSupport http() {
        return new HttpSupport(port);
    }

    private Map<String, Object> session(String refresh) {
        return db.queryForMap("SELECT * FROM user_sessions WHERE refresh_token = ?", refresh);
    }

    private Claims claims(String token) {
        var key = new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private HttpSupport.Resp login(String user, String pwd, String device) {
        return device == null
                ? http().post("/auth/login", json("username", user, "password", pwd))
                : http().post("/auth/login", json("username", user, "password", pwd, "deviceId", device));
    }

    @Test
    void loginRefreshReuseScenario() {
        var login = login("ivan", "Ivan12345", "laptop");
        assertThat(login.status()).isEqualTo(200);
        String access1 = login.text("accessToken");
        String refresh1 = login.text("refreshToken");
        assertThat(access1).isNotBlank();
        assertThat(refresh1).isNotBlank();
        assertThat(login.json().size()).isEqualTo(2);

        // Сессия ACTIVE, сроки совпадают с exp токенов
        var s1 = session(refresh1);
        assertThat(s1.get("status")).isEqualTo("ACTIVE");
        assertThat(s1.get("username")).isEqualTo("ivan");
        assertThat(s1.get("device_id")).isEqualTo("laptop");
        assertThat(s1.get("access_token")).isEqualTo(access1);
        Claims a = claims(access1), r = claims(refresh1);
        assertThat(a.get("type")).isEqualTo("access");
        assertThat(r.get("type")).isEqualTo("refresh");
        assertThat(a.getId()).isNotEqualTo(r.getId());
        assertThat(toInstant(s1.get("access_token_expiry"))).isEqualTo(a.getExpiration().toInstant());
        assertThat(toInstant(s1.get("refresh_token_expiry"))).isEqualTo(r.getExpiration().toInstant());

        // Защищённый endpoint с access
        var me = http().get("/api/auth/me", access1);
        assertThat(me.status()).isEqualTo(200);
        assertThat(me.text("username")).isEqualTo("ivan");
        // refresh в Bearer не принимается
        assertThat(http().get("/api/auth/me", refresh1).status()).isEqualTo(401);
        // access в /auth/refresh не принимается
        assertThat(http().post("/auth/refresh", json("refreshToken", access1)).status()).isEqualTo(401);

        // Обновление
        var ref = http().post("/auth/refresh", json("refreshToken", refresh1));
        assertThat(ref.status()).isEqualTo(200);
        String refresh2 = ref.text("refreshToken");
        assertThat(refresh2).isNotEqualTo(refresh1);
        assertThat(ref.text("accessToken")).isNotEqualTo(access1);
        assertThat(session(refresh1).get("status")).isEqualTo("USED");
        var s2 = session(refresh2);
        assertThat(s2.get("status")).isEqualTo("ACTIVE");
        assertThat(s2.get("device_id")).isEqualTo("laptop");
        assertThat(s2.get("id")).isNotEqualTo(s1.get("id"));
        assertThat(http().get("/api/auth/me", ref.text("accessToken")).status()).isEqualTo(200);

        // Повторное предъявление использованного refresh -> 401 и REVOKED
        var reuse = http().post("/auth/refresh", json("refreshToken", refresh1));
        assertThat(reuse.status()).isEqualTo(401);
        assertThat(reuse.text("accessToken")).isNull();
        assertThat(session(refresh1).get("status")).isEqualTo("REVOKED");

        // Отозванный -> 401, остаётся REVOKED
        assertThat(http().post("/auth/refresh", json("refreshToken", refresh1)).status()).isEqualTo(401);
        assertThat(session(refresh1).get("status")).isEqualTo("REVOKED");

        // Новая пара продолжает работать (отзыв цепочки не требуется)
        assertThat(http().post("/auth/refresh", json("refreshToken", refresh2)).status()).isEqualTo(200);
    }

    @Test
    void loginErrors() {
        assertThat(login("lab", "wrong", null).status()).isEqualTo(401);
        assertThat(login("nobody", "Whatever1", null).status()).isEqualTo(401);
        assertThat(login("blocked", "Blocked123", null).status()).isEqualTo(401);
        var missing = http().post("/auth/login", json("username", "lab"));
        assertThat(missing.status()).isEqualTo(400);
        assertThat(missing.text("accessToken")).isNull();
        assertThat(http().post("/auth/login", json("username", "", "password", "x")).status()).isEqualTo(400);
        assertThat(http().post("/auth/login", "").status()).isEqualTo(400);
    }

    @Test
    void refreshErrors() {
        assertThat(http().post("/auth/refresh", "{}").status()).isEqualTo(400);
        assertThat(http().post("/auth/refresh", json("refreshToken", " ")).status()).isEqualTo(400);
        assertThat(http().post("/auth/refresh", json("refreshToken", "garbage")).status()).isEqualTo(401);

        var key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        // Подписан верным ключом, но истёк
        String expired = Jwts.builder().id(UUID.randomUUID().toString()).subject("lab")
                .issuedAt(Date.from(now.minusSeconds(100))).expiration(Date.from(now.minusSeconds(10)))
                .claim("type", "refresh").signWith(key, Jwts.SIG.HS256).compact();
        assertThat(http().post("/auth/refresh", json("refreshToken", expired)).status()).isEqualTo(401);
        // Верный JWT, но сессии нет в БД
        String unknown = Jwts.builder().id(UUID.randomUUID().toString()).subject("lab")
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(600)))
                .claim("type", "refresh").signWith(key, Jwts.SIG.HS256).compact();
        assertThat(http().post("/auth/refresh", json("refreshToken", unknown)).status()).isEqualTo(401);
        // Без jti
        String noJti = Jwts.builder().subject("lab")
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(600)))
                .claim("type", "access").signWith(key, Jwts.SIG.HS256).compact();
        assertThat(http().get("/api/auth/me", noJti).status()).isEqualTo(401);
        // Другой алгоритм
        var key512 = Keys.hmacShaKeyFor((JWT_SECRET + JWT_SECRET).getBytes(StandardCharsets.UTF_8));
        String hs512 = Jwts.builder().id("x").subject("lab")
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(600)))
                .claim("type", "access").signWith(key512, Jwts.SIG.HS512).compact();
        assertThat(http().get("/api/auth/me", hs512).status()).isEqualTo(401);
    }

    @Test
    void registerIssuesPair() {
        String username = "reg_" + System.nanoTime();
        var reg = http().post("/auth/register",
                "{\"name\":\"Тест\",\"username\":\"" + username + "\",\"password\":\"Passw0rd1\",\"role\":\"LAB\"}");
        assertThat(reg.status()).isEqualTo(201);
        assertThat(session(reg.text("refreshToken")).get("status")).isEqualTo("ACTIVE");
        var me = http().get("/api/auth/me", reg.text("accessToken"));
        assertThat(me.status()).isEqualTo(200);
        assertThat(me.text("role")).isEqualTo("CUSTOMER");
        assertThat(me.json().has("password")).isFalse();

        var dup = http().post("/auth/register", json("name", "Дубль", "username", username, "password", "Passw0rd1"));
        assertThat(dup.status()).isEqualTo(409);
        assertThat(dup.text("accessToken")).isNull();
        assertThat(http().post("/auth/register", json("name", "Слабый", "username", "w" + username, "password", "123"))
                .status()).isEqualTo(400);
        assertThat(http().post("/auth/register", json("username", "n" + username, "password", "Passw0rd1"))
                .status()).isEqualTo(400);
    }

    @Test
    void blockedUserCannotUseAccessOrRefresh() {
        String username = "blk_" + System.nanoTime();
        var reg = http().post("/auth/register", json("name", "B", "username", username, "password", "Passw0rd1"));
        assertThat(reg.status()).isEqualTo(201);
        db.update("UPDATE users SET enabled = false WHERE username = ?", username);
        assertThat(http().get("/api/auth/me", reg.text("accessToken")).status()).isEqualTo(401);
        assertThat(http().post("/auth/refresh", json("refreshToken", reg.text("refreshToken"))).status()).isEqualTo(401);
        assertThat(session(reg.text("refreshToken")).get("status")).isEqualTo("ACTIVE");
    }

    @Test
    void concurrentRefreshOnlyOneSucceeds() throws Exception {
        String refresh = login("anna", "Anna12345", null).text("refreshToken");
        int n = 8;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            results.add(pool.submit(() -> {
                start.await();
                return http().post("/auth/refresh", json("refreshToken", refresh)).status();
            }));
        }
        start.countDown();
        List<Integer> statuses = new ArrayList<>();
        for (var f : results) statuses.add(f.get(30, TimeUnit.SECONDS));
        pool.shutdown();
        assertThat(statuses).filteredOn(s -> s == 200).hasSize(1);
        assertThat(statuses).filteredOn(s -> s == 401).hasSize(n - 1);
    }

    private static Instant toInstant(Object v) {
        if (v instanceof java.sql.Timestamp t) return t.toInstant();
        if (v instanceof java.time.OffsetDateTime o) return o.toInstant();
        throw new IllegalArgumentException(String.valueOf(v));
    }
}
