package com.example.demo.service;

import com.example.demo.model.Analysis;
import com.example.demo.model.Measurement;
import com.example.demo.repository.AnalysisRepository;
import com.example.demo.repository.MeasurementRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService extends BaseService<Analysis> {

    private final MeasurementRepository measurementRepository;
    private final MeasurementService measurementService;

    public AnalysisService(AnalysisRepository repository,
                           MeasurementRepository measurementRepository,
                           MeasurementService measurementService) {
        super(repository);
        this.measurementRepository = measurementRepository;
        this.measurementService = measurementService;
    }

    @Override
    protected void patch(Analysis target, Analysis data) {
        if (data.sampleId != null) target.sampleId = data.sampleId;
        if (data.methodId != null) target.methodId = data.methodId;
        if (data.status != null) target.status = data.status;
        if (data.averageValue != null) target.averageValue = data.averageValue;
    }

    @Override
    protected void beforeDelete(Long id) {
        for (Measurement m : measurementRepository.findAll()) {
            if (id.equals(m.analysisId)) {
                measurementService.delete(m.id);
            }
        }
    }
}