package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Refresh отклонён: недействительный JWT, неизвестная/использованная/отозванная сессия → 401. */
public class InvalidRefreshTokenException extends ResponseStatusException {
    public InvalidRefreshTokenException(String reason) {
        super(HttpStatus.UNAUTHORIZED, reason);
    }
}
