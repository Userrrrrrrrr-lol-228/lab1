package com.example.demo.controller;

import com.example.demo.model.Analysis;
import com.example.demo.model.Measurement;
import com.example.demo.model.Sample;
import com.example.demo.service.LabService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LabController {

    private final LabService lab;

    public LabController(LabService lab) {
        this.lab = lab;
    }

    @PostMapping("/analyses/{id}/start")
    @PreAuthorize("hasRole('LAB')")
    public Analysis start(@PathVariable Long id) {
        return lab.start(id);
    }

    @PostMapping("/analyses/{id}/measurements")
    @PreAuthorize("hasRole('LAB')")
    @ResponseStatus(HttpStatus.CREATED)
    public Measurement addMeasurement(@PathVariable Long id, @RequestBody Measurement body) {
        return lab.addMeasurement(id, body.value);
    }

    @PostMapping("/measurements/{id}/confirm")
    @PreAuthorize("hasRole('LAB')")
    public Measurement confirm(@PathVariable Long id) {
        return lab.confirm(id);
    }

    @PostMapping("/analyses/{id}/complete")
    @PreAuthorize("hasRole('LAB')")
    public Analysis complete(@PathVariable Long id) {
        return lab.complete(id);
    }

    @PostMapping("/samples/{id}/reject")
    @PreAuthorize("hasRole('LAB') or @access.ownsSample(#id)")
    public Sample reject(@PathVariable Long id) {
        return lab.reject(id);
    }
}
