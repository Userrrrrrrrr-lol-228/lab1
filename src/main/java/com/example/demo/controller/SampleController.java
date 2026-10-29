package com.example.demo.controller;

import com.example.demo.model.Sample;
import com.example.demo.service.SampleService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/samples")
public class SampleController extends BaseController<Sample> {

    public SampleController(SampleService service) {
        super(service);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LAB') or @access.ownsSample(#id)")
    public Sample getOne(@PathVariable Long id) {
        return super.getOne(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LAB') or #body.customerId == @access.currentUserId()")
    public Sample create(@RequestBody Sample body) {
        return super.create(body);
    }
}
