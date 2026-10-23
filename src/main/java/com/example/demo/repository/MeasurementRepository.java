package com.example.demo.repository;

import com.example.demo.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    List<Measurement> findByAnalysisIdAndConfirmedTrue(Long analysisId);
}
