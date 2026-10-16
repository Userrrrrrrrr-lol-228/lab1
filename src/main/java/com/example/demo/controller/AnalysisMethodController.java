package com.example.demo.controller;

import com.example.demo.model.AnalysisMethod;
import com.example.demo.service.AnalysisMethodService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis-methods")
public class AnalysisMethodController extends BaseController<AnalysisMethod> {

    public AnalysisMethodController(AnalysisMethodService service) {
        super(service);
    }
}