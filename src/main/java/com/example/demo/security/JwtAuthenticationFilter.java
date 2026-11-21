package com.example.demo.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Аутентификация по заголовку {@code Authorization: Bearer <access>}.
 * Принимает только access-токены; refresh в этом заголовке отклоняется (401).
 * Не регистрируется как @Component, чтобы не попасть в цепочку сервлет-фильтров дважды.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwt;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwt, UserDetailsService userDetailsService) {
        this.jwt = jwt;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // /auth/login, /auth/register, /auth/refresh не требуют access-токена.
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            chain.doFilter(req, res);
            return;
        }
        UserDetails user;
        try {
            Claims claims = jwt.validateAccessToken(header.substring(BEARER.length()).trim());
            user = userDetailsService.loadUserByUsername(claims.getSubject());
        } catch (JwtValidationException | UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            JsonError.write(res, 401, "Недействительный access-токен");
            return;
        }
        if (!user.isEnabled()) {
            SecurityContextHolder.clearContext();
            JsonError.write(res, 401, "Учётная запись заблокирована");
            return;
        }
        var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        chain.doFilter(req, res);
    }
}
