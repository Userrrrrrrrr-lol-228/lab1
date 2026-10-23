package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_methods")
public class AnalysisMethod extends BaseEntity {
    public String name;
    public String allowedMaterial;
    public Double minValue;
    public Double maxValue;
    public Double maxSpread;
}
