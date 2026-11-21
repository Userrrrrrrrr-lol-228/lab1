#!/bin/bash
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<SQL
CREATE ROLE app_user LOGIN PASSWORD '$APP_DB_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON users, analysis_methods, samples, analyses, measurements TO app_user;
-- Сессии приложение только читает, создаёт и меняет им статус; удалять их не нужно.
GRANT SELECT, INSERT, UPDATE ON user_sessions TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
SQL
