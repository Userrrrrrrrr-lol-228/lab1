package com.example.demo.service;

/**
 * Повторно предъявлен уже использованный refresh. Сессия переводится в REVOKED,
 * и это изменение фиксируется, хотя клиент получает отказ (см. noRollbackFor в TokenService).
 */
public class RefreshTokenReuseException extends InvalidRefreshTokenException {
    public RefreshTokenReuseException() {
        super("Refresh-токен уже использован, сессия отозвана");
    }
}
