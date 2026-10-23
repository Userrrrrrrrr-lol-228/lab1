package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "samples")
public class Sample extends BaseEntity {
    public String material;
    public Long customerId;
    public LocalDate receivedDate;
    public String status;
}
