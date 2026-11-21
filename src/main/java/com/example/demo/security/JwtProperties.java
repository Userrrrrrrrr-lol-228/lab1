package com.example.demo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Настройки JWT: секрет HS256 и время жизни токенов. */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, Duration accessTtl, Duration refreshTtl) {
}
