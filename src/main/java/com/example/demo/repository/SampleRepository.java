package com.example.demo.repository;

import com.example.demo.model.Sample;
import org.springframework.stereotype.Repository;

@Repository
public class SampleRepository extends InMemoryRepository<Sample> {
}