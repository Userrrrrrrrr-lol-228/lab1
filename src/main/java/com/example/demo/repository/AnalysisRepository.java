package com.example.demo.repository;

import com.example.demo.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findBySampleId(Long sampleId);
}
