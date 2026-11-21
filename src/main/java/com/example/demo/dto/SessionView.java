package com.example.demo.dto;

import com.example.demo.model.SessionStatus;
import com.example.demo.model.UserSession;

import java.time.Instant;
import java.util.UUID;

/** Сессия без самих токенов — для проверки статуса. */
public record SessionView(UUID id, String username, String deviceId, SessionStatus status,
                          Instant accessTokenExpiry, Instant refreshTokenExpiry) {

    public static SessionView of(UserSession s) {
        return new SessionView(s.id, s.username, s.deviceId, s.status, s.accessTokenExpiry, s.refreshTokenExpiry);
    }
}
