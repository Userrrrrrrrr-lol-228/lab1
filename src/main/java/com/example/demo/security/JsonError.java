package com.example.demo.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class JsonError {

    private JsonError() {
    }

    static void write(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
