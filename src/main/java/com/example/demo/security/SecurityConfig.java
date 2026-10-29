package com.example.demo.security;

import com.example.demo.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(c -> c
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
                .httpBasic(b -> b.authenticationEntryPoint((req, res, e) ->
                        JsonError.write(res, 401, "Требуется аутентификация")))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                JsonError.write(res, 401, "Требуется аутентификация"))
                        .accessDeniedHandler((req, res, ex) -> JsonError.write(res, 403,
                                ex instanceof CsrfException ? "CSRF-токен отсутствует или неверен" : "Недостаточно прав")))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me", "/api/analysis-methods/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/samples", "/api/samples/*/reject").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/samples/*").authenticated()
                        .anyRequest().hasRole("LAB"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username)
                .map(u -> User.withUsername(u.username)
                        .password(u.password)
                        .roles(u.role)
                        .disabled(!u.enabled)
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
