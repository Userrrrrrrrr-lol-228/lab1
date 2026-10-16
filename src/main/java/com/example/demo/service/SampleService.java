package com.example.demo.service;

import com.example.demo.model.Analysis;
import com.example.demo.model.Sample;
import com.example.demo.repository.AnalysisRepository;
import com.example.demo.repository.SampleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SampleService extends BaseService<Sample> {

    private final AnalysisRepository analysisRepository;
    private final AnalysisService analysisService;

    public SampleService(SampleRepository repository,
                         AnalysisRepository analysisRepository,
                         AnalysisService analysisService) {
        super(repository);
        this.analysisRepository = analysisRepository;
        this.analysisService = analysisService;
    }

    @Override
    protected void beforeCreate(Sample s) {
        s.receivedDate = LocalDate.now();
    }

    @Override
    protected void patch(Sample target, Sample data) {
        if (data.material != null) target.material = data.material;
        if (data.customerId != null) target.customerId = data.customerId;
        if (data.status != null) target.status = data.status;
    }

    @Override
    protected void beforeDelete(Long id) {
        for (Analysis a : analysisRepository.findAll()) {
            if (id.equals(a.sampleId)) {
                analysisService.delete(a.id);
            }
        }
    }
}