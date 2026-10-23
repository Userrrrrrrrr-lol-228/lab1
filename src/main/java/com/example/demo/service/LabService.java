package com.example.demo.service;

import com.example.demo.model.Analysis;
import com.example.demo.model.AnalysisMethod;
import com.example.demo.model.Measurement;
import com.example.demo.model.Sample;
import com.example.demo.repository.AnalysisMethodRepository;
import com.example.demo.repository.AnalysisRepository;
import com.example.demo.repository.MeasurementRepository;
import com.example.demo.repository.SampleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class LabService {

    private final SampleRepository samples;
    private final AnalysisRepository analyses;
    private final AnalysisMethodRepository methods;
    private final MeasurementRepository measurements;

    public LabService(SampleRepository samples, AnalysisRepository analyses,
                      AnalysisMethodRepository methods, MeasurementRepository measurements) {
        this.samples = samples;
        this.analyses = analyses;
        this.methods = methods;
        this.measurements = measurements;
    }

    private static ResponseStatusException conflict(String msg) {
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }

    private static <T> T find(JpaRepository<T, Long> repo, Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Analysis start(Long analysisId) {
        Analysis a = find(analyses, analysisId);
        Sample s = find(samples, a.sampleId);
        AnalysisMethod m = find(methods, a.methodId);
        if (!"NEW".equals(a.status)) throw conflict("Анализ уже запущен или закрыт");
        if ("DONE".equals(s.status) || "REJECTED".equals(s.status)) throw conflict("Образец закрыт");
        if (!m.allowedMaterial.equals(s.material)) throw conflict("Метод не подходит для материала образца");
        a.status = "IN_PROGRESS";
        s.status = "IN_ANALYSIS";
        return a;
    }

    @Transactional
    public Measurement addMeasurement(Long analysisId, Double value) {
        Analysis a = find(analyses, analysisId);
        AnalysisMethod m = find(methods, a.methodId);
        if (!"IN_PROGRESS".equals(a.status)) throw conflict("Анализ не в работе");
        if (value == null || value < m.minValue || value > m.maxValue) {
            throw conflict("Значение вне диапазона метода");
        }
        Measurement x = new Measurement();
        x.analysisId = a.id;
        x.value = value;
        x.time = LocalDateTime.now();
        x.confirmed = false;
        return measurements.save(x);
    }

    @Transactional
    public Measurement confirm(Long measurementId) {
        Measurement x = find(measurements, measurementId);
        Analysis a = find(analyses, x.analysisId);
        if (!"IN_PROGRESS".equals(a.status)) throw conflict("Анализ не в работе");
        if (Boolean.TRUE.equals(x.confirmed)) throw conflict("Измерение уже подтверждено");
        x.confirmed = true;
        return x;
    }

    @Transactional
    public Analysis complete(Long analysisId) {
        Analysis a = find(analyses, analysisId);
        AnalysisMethod m = find(methods, a.methodId);
        Sample s = find(samples, a.sampleId);
        if (!"IN_PROGRESS".equals(a.status)) throw conflict("Анализ не в работе");

        List<Measurement> confirmed = measurements.findByAnalysisIdAndConfirmedTrue(a.id);
        if (confirmed.size() < 2) throw conflict("Нужно минимум 2 подтверждённых измерения");

        DoubleSummaryStatistics st = confirmed.stream().mapToDouble(x -> x.value).summaryStatistics();
        if (st.getMax() - st.getMin() > m.maxSpread) {
            a.status = "FAILED";
            return a;
        }
        a.status = "DONE";
        a.averageValue = st.getAverage();

        boolean allDone = analyses.findBySampleId(s.id).stream()
                .filter(x -> !"CANCELLED".equals(x.status))
                .allMatch(x -> "DONE".equals(x.status));
        if (allDone) s.status = "DONE";
        return a;
    }

    @Transactional
    public Sample reject(Long sampleId) {
        Sample s = find(samples, sampleId);
        if ("DONE".equals(s.status) || "REJECTED".equals(s.status)) {
            throw conflict("Образец уже завершён или отклонён");
        }
        s.status = "REJECTED";
        for (Analysis a : analyses.findBySampleId(s.id)) {
            if ("NEW".equals(a.status) || "IN_PROGRESS".equals(a.status)) a.status = "CANCELLED";
        }
        return s;
    }
}
