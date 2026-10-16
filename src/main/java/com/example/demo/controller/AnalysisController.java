package com.example.demo.controller;

import com.example.demo.model.Analysis;
import com.example.demo.service.AnalysisService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController extends BaseController<Analysis> {

    public AnalysisController(AnalysisService service) {
        super(service);
    }
}