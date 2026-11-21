package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.TokenPair;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService auth;
    private final UserService users;

    public AuthController(AuthService auth, UserService users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/auth/login")
    public TokenPair login(@RequestBody LoginRequest body) {
        return auth.login(body);
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenPair register(@RequestBody RegisterRequest body) {
        return auth.register(body);
    }

    @PostMapping("/auth/refresh")
    public TokenPair refresh(@RequestBody RefreshRequest body) {
        return auth.refresh(body);
    }

    /** Защищённый endpoint: требует access-токен в заголовке Authorization: Bearer. */
    @GetMapping("/api/auth/me")
    public User me(Authentication authentication) {
        return users.getByUsername(authentication.getName());
    }
}
