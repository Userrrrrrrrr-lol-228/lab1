package com.example.demo.service;

import com.example.demo.model.Analysis;
import com.example.demo.repository.AnalysisRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService extends BaseService<Analysis> {

    public AnalysisService(AnalysisRepository repository) {
        super(repository);
    }

    @Override
    protected void beforeCreate(Analysis a) {
        a.status = "NEW";
        a.averageValue = null;
    }

    @Override
    protected void patch(Analysis target, Analysis data) {
        if (data.sampleId != null) target.sampleId = data.sampleId;
        if (data.methodId != null) target.methodId = data.methodId;
        if (data.status != null) target.status = data.status;
        if (data.averageValue != null) target.averageValue = data.averageValue;
    }
}
