package com.example.demo.controller;

import com.example.demo.model.Sample;
import com.example.demo.service.SampleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/samples")
public class SampleController extends BaseController<Sample> {

    public SampleController(SampleService service) {
        super(service);
    }
}