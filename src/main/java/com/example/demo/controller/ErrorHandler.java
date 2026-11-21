package com.example.demo.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> integrity(DataIntegrityViolationException e) {
        return Map.of("error", "Нарушено ограничение целостности данных");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> unreadable(HttpMessageNotReadableException e) {
        return Map.of("error", "Тело запроса отсутствует или содержит некорректный JSON");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        String msg = e.getReason() != null ? e.getReason() : HttpStatus.valueOf(e.getStatusCode().value()).getReasonPhrase();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", msg));
    }
}
