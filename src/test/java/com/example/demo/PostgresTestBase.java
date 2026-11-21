package com.example.demo;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Поднимает встроенный PostgreSQL, накатывает db/01..03 так же, как docker-compose,
 * и подключает приложение под ограниченной ролью app_user.
 */
public abstract class PostgresTestBase {

    static final String APP_PASSWORD = "app_test_pwd";
    static final String JWT_SECRET = "test-secret-test-secret-test-secret-0123456789";
    static final EmbeddedPostgres PG;

    static {
        try {
            PG = EmbeddedPostgres.builder().start();
            try (Connection c = PG.getPostgresDatabase().getConnection(); Statement st = c.createStatement()) {
                st.execute(Files.readString(Path.of("db/01-schema.sql")));
                st.execute(Files.readString(Path.of("db/02-data.sql")));
                String roles = Files.readString(Path.of("db/03-roles.sh"));
                String sql = roles.substring(roles.indexOf("<<SQL") + 5, roles.lastIndexOf("SQL"))
                        .replace("$APP_DB_PASSWORD", APP_PASSWORD);
                st.execute(sql);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> PG.getJdbcUrl("app_user", "postgres"));
        r.add("spring.datasource.username", () -> "app_user");
        r.add("spring.datasource.password", () -> APP_PASSWORD);
        r.add("jwt.secret", () -> JWT_SECRET);
    }
}
