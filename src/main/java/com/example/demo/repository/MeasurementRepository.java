package com.example.demo.repository;

import com.example.demo.model.Measurement;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementRepository extends InMemoryRepository<Measurement> {
}