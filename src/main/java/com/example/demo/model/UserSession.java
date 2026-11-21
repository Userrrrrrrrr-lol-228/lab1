package com.example.demo.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** Одна запись — одна выданная пара access/refresh. */
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "username", nullable = false)
    public String username;

    @Column(name = "device_id")
    public String deviceId;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    public String accessToken;

    @Column(name = "refresh_token", nullable = false, unique = true, columnDefinition = "TEXT")
    public String refreshToken;

    @Column(name = "access_token_expiry", nullable = false)
    public Instant accessTokenExpiry;

    @Column(name = "refresh_token_expiry", nullable = false)
    public Instant refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public SessionStatus status;
}
