package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User> {

    public UserService(UserRepository repository) {
        super(repository);
    }

    @Override
    protected void patch(User target, User data) {
        if (data.name != null) target.name = data.name;
    }
}
