package com.example.demo.service;

import com.example.demo.model.Measurement;
import com.example.demo.repository.MeasurementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MeasurementService extends BaseService<Measurement> {

    public MeasurementService(MeasurementRepository repository) {
        super(repository);
    }

    @Override
    protected void beforeCreate(Measurement m) {
        m.time = LocalDateTime.now();
    }

    @Override
    protected void patch(Measurement target, Measurement data) {
        if (data.analysisId != null) target.analysisId = data.analysisId;
        if (data.value != null) target.value = data.value;
        if (data.confirmed != null) target.confirmed = data.confirmed;
    }
}