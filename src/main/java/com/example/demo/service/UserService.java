package com.example.demo.service;

import com.example.demo.model.Sample;
import com.example.demo.model.User;
import com.example.demo.repository.SampleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User> {

    private final SampleRepository sampleRepository;
    private final SampleService sampleService;

    public UserService(UserRepository repository,
                       SampleRepository sampleRepository,
                       SampleService sampleService) {
        super(repository);
        this.sampleRepository = sampleRepository;
        this.sampleService = sampleService;
    }

    @Override
    protected void patch(User target, User data) {
        if (data.name != null) target.name = data.name;
    }

    @Override
    protected void beforeDelete(Long id) {
        for (Sample s : sampleRepository.findAll()) {
            if (id.equals(s.customerId)) {
                sampleService.delete(s.id);
            }
        }
    }
}