package com.example.demo.service;

import com.example.demo.model.Sample;
import com.example.demo.repository.SampleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SampleService extends BaseService<Sample> {

    public SampleService(SampleRepository repository) {
        super(repository);
    }

    @Override
    protected void beforeCreate(Sample s) {
        s.receivedDate = LocalDate.now();
        s.status = "NEW";
    }

    @Override
    protected void patch(Sample target, Sample data) {
        if (data.material != null) target.material = data.material;
        if (data.customerId != null) target.customerId = data.customerId;
        if (data.status != null) target.status = data.status;
    }
}
