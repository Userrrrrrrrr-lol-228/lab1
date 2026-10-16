package com.example.demo.model;

import java.time.LocalDateTime;

public class Measurement extends BaseEntity {
    public Long analysisId;
    public Double value;
    public LocalDateTime time;
    public Boolean confirmed;
}