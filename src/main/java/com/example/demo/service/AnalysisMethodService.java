package com.example.demo.service;

import com.example.demo.model.AnalysisMethod;
import com.example.demo.repository.AnalysisMethodRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMethodService extends BaseService<AnalysisMethod> {

    public AnalysisMethodService(AnalysisMethodRepository repository) {
        super(repository);
    }

    @Override
    protected void patch(AnalysisMethod target, AnalysisMethod data) {
        if (data.name != null) target.name = data.name;
        if (data.allowedMaterial != null) target.allowedMaterial = data.allowedMaterial;
        if (data.minValue != null) target.minValue = data.minValue;
        if (data.maxValue != null) target.maxValue = data.maxValue;
        if (data.maxSpread != null) target.maxSpread = data.maxSpread;
    }
}
