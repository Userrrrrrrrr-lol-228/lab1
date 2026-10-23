package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "analyses")
public class Analysis extends BaseEntity {
    public Long sampleId;
    public Long methodId;
    public String status;
    public Double averageValue;
}
