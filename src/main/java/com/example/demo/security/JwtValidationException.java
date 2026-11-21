package com.example.demo.security;

/** Токен не прошёл проверку: подпись, срок, обязательные claims или type. */
public class JwtValidationException extends RuntimeException {
    public JwtValidationException(String message) {
        super(message);
    }
}
