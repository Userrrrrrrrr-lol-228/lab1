package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "measurements")
public class Measurement extends BaseEntity {
    public Long analysisId;
    public Double value;
    @Column(name = "measured_at")
    public LocalDateTime time;
    public Boolean confirmed;
}
