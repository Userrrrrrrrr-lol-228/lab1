package com.example.demo;

import com.example.demo.model.UserSession;
import com.example.demo.repository.UserSessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static com.example.demo.HttpSupport.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/** Ошибка сохранения новой сессии откатывает регистрацию и перевод старой сессии в USED. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionRollbackTests extends PostgresTestBase {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate db;

    @MockitoSpyBean
    UserSessionRepository sessions;

    @AfterEach
    void reset() {
        Mockito.reset(sessions);
    }

    private void failNextSessionSave() {
        doThrow(new DataAccessResourceFailureException("simulated"))
                .when(sessions).saveAndFlush(any(UserSession.class));
    }

    @Test
    void registrationRolledBackWhenSessionNotSaved() {
        failNextSessionSave();
        String username = "rb_" + System.nanoTime();
        var r = new HttpSupport(port).post("/auth/register",
                json("name", "RB", "username", username, "password", "Passw0rd1"));
        assertThat(r.status()).isGreaterThanOrEqualTo(500);
        assertThat(r.text("accessToken")).isNull();
        assertThat(db.queryForObject("SELECT count(*) FROM users WHERE username = ?", Integer.class, username))
                .isZero();
    }

    @Test
    void refreshRolledBackWhenNewSessionNotSaved() {
        var http = new HttpSupport(port);
        String refresh = http.post("/auth/login", json("username", "lab", "password", "Lab12345")).text("refreshToken");
        failNextSessionSave();
        var r = http.post("/auth/refresh", json("refreshToken", refresh));
        assertThat(r.status()).isGreaterThanOrEqualTo(500);
        assertThat(r.text("accessToken")).isNull();
        assertThat(db.queryForObject("SELECT status FROM user_sessions WHERE refresh_token = ?", String.class, refresh))
                .isEqualTo("ACTIVE");
        Mockito.reset(sessions);
        assertThat(http.post("/auth/refresh", json("refreshToken", refresh)).status()).isEqualTo(200);
    }
}
