package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService extends BaseService<User> {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        super(repository);
        this.users = repository;
        this.encoder = encoder;
    }

    public User register(User u) {
        u.role = "CUSTOMER";
        u.enabled = true;
        return create(u);
    }

    public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    protected void beforeCreate(User u) {
        if (u.name == null || u.name.isBlank() || u.username == null || u.username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите имя и логин");
        }
        if (u.password == null || !u.password.matches("(?=.*\\p{L})(?=.*\\d).{8,}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пароль должен быть не короче 8 символов и содержать буквы и цифры");
        }
        if (users.existsByUsername(u.username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Логин уже занят");
        }
        u.password = encoder.encode(u.password);
        if (u.role == null) u.role = "CUSTOMER";
        if (u.enabled == null) u.enabled = true;
    }

    @Override
    protected void patch(User target, User data) {
        if (data.name != null) target.name = data.name;
        if (data.role != null) target.role = data.role;
        if (data.enabled != null) target.enabled = data.enabled;
    }
}
