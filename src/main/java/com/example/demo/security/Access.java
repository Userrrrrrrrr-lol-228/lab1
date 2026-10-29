package com.example.demo.security;

import com.example.demo.repository.SampleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("access")
public class Access {

    private final UserRepository users;
    private final SampleRepository samples;

    public Access(UserRepository users, SampleRepository samples) {
        this.users = users;
        this.samples = samples;
    }

    public Long currentUserId() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByUsername(name).map(u -> u.id).orElse(null);
    }

    public boolean ownsSample(Long sampleId) {
        Long me = currentUserId();
        return samples.findById(sampleId).map(s -> s.customerId.equals(me)).orElse(false);
    }
}
