package com.example.demo.controller;

import com.example.demo.dto.RefreshRequest;
import com.example.demo.dto.SessionView;
import com.example.demo.repository.UserSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Только для LAB: посмотреть состояние сессии по refresh-токену (сами токены не возвращаются). */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final UserSessionRepository sessions;

    public SessionController(UserSessionRepository sessions) {
        this.sessions = sessions;
    }

    @PostMapping("/lookup")
    public SessionView lookup(@RequestBody RefreshRequest body) {
        if (body == null || body.refreshToken() == null || body.refreshToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите refreshToken");
        }
        return sessions.findByRefreshToken(body.refreshToken())
                .map(SessionView::of)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сессия не найдена"));
    }
}
