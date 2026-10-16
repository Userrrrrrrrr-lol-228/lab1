package com.example.demo.controller;

import com.example.demo.model.Measurement;
import com.example.demo.service.MeasurementService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController extends BaseController<Measurement> {

    public MeasurementController(MeasurementService service) {
        super(service);
    }
}