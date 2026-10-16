package com.example.demo.service;

import com.example.demo.model.Analysis;
import com.example.demo.model.AnalysisMethod;
import com.example.demo.repository.AnalysisMethodRepository;
import com.example.demo.repository.AnalysisRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMethodService extends BaseService<AnalysisMethod> {

    private final AnalysisRepository analysisRepository;
    private final AnalysisService analysisService;

    public AnalysisMethodService(AnalysisMethodRepository repository,
                                 AnalysisRepository analysisRepository,
                                 AnalysisService analysisService) {
        super(repository);
        this.analysisRepository = analysisRepository;
        this.analysisService = analysisService;
    }

    @Override
    protected void patch(AnalysisMethod target, AnalysisMethod data) {
        if (data.name != null) target.name = data.name;
        if (data.allowedMaterial != null) target.allowedMaterial = data.allowedMaterial;
        if (data.minValue != null) target.minValue = data.minValue;
        if (data.maxValue != null) target.maxValue = data.maxValue;
        if (data.maxSpread != null) target.maxSpread = data.maxSpread;
    }

    @Override
    protected void beforeDelete(Long id) {
        for (Analysis a : analysisRepository.findAll()) {
            if (id.equals(a.methodId)) {
                analysisService.delete(a.id);
            }
        }
    }
}