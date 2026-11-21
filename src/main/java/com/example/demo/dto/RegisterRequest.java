package com.example.demo.dto;

/** Поле role (если передано) не читается: регистрация всегда создаёт CUSTOMER. */
public record RegisterRequest(String name, String username, String password, String deviceId) {
}
