CREATE TABLE users (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role     VARCHAR(20)  NOT NULL CHECK (role IN ('CUSTOMER', 'LAB')),
    enabled  BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE analysis_methods (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,
    allowed_material VARCHAR(100) NOT NULL,
    min_value        DOUBLE PRECISION NOT NULL,
    max_value        DOUBLE PRECISION NOT NULL,
    max_spread       DOUBLE PRECISION NOT NULL CHECK (max_spread >= 0),
    CHECK (min_value < max_value)
);

CREATE TABLE samples (
    id            BIGSERIAL PRIMARY KEY,
    material      VARCHAR(100) NOT NULL,
    customer_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    received_date DATE NOT NULL,
    status        VARCHAR(20) NOT NULL
        CHECK (status IN ('NEW', 'IN_ANALYSIS', 'DONE', 'REJECTED'))
);

CREATE TABLE analyses (
    id            BIGSERIAL PRIMARY KEY,
    sample_id     BIGINT NOT NULL REFERENCES samples(id) ON DELETE CASCADE,
    method_id     BIGINT NOT NULL REFERENCES analysis_methods(id) ON DELETE CASCADE,
    status        VARCHAR(20) NOT NULL
        CHECK (status IN ('NEW', 'IN_PROGRESS', 'DONE', 'FAILED', 'CANCELLED')),
    average_value DOUBLE PRECISION,
    UNIQUE (sample_id, method_id)
);

CREATE TABLE measurements (
    id          BIGSERIAL PRIMARY KEY,
    analysis_id BIGINT NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    value       DOUBLE PRECISION NOT NULL,
    measured_at TIMESTAMP NOT NULL,
    confirmed   BOOLEAN NOT NULL
);

-- Задание 5: одна строка = одна выданная пара access/refresh.
CREATE TABLE user_sessions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username             VARCHAR(50) NOT NULL
        REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE,
    device_id            VARCHAR(255),
    access_token         TEXT NOT NULL,
    refresh_token        TEXT NOT NULL UNIQUE,
    access_token_expiry  TIMESTAMPTZ NOT NULL,
    refresh_token_expiry TIMESTAMPTZ NOT NULL,
    status               VARCHAR(20) NOT NULL
        CHECK (status IN ('ACTIVE', 'USED', 'REVOKED'))
);

CREATE INDEX user_sessions_username_idx ON user_sessions (username);
