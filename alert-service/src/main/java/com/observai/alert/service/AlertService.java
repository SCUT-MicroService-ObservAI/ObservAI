package com.observai.alert.service;

import com.observai.alert.model.AlertDetail;
import com.observai.alert.model.AlertRecord;
import com.observai.alert.repository.AlertRepository;
import com.observai.alert.repository.AlertStatusHistoryRepository;
import com.observai.common.dto.AlertCreateRequest;
import com.observai.common.dto.AlertCreateResponse;
import com.observai.common.dto.AlertStatusUpdateRequest;
import com.observai.common.enums.AlertStatus;
import com.observai.common.enums.DiagnosisStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final AlertStatusHistoryRepository historyRepository;
    private final DiagnosisService diagnosisService;

    public AlertService(AlertRepository alertRepository, AlertStatusHistoryRepository historyRepository,
                        DiagnosisService diagnosisService) {
        this.alertRepository = alertRepository;
        this.historyRepository = historyRepository;
        this.diagnosisService = diagnosisService;
    }

    public AlertCreateResponse createOrUpdate(AlertCreateRequest request) {
        String fingerprint = fingerprint(request.serviceName(), request.alertType(), request.metricName());
        AlertRecord alert = alertRepository.findOpenByFingerprint(fingerprint)
                .map(existing -> updateDuplicated(existing, request))
                .orElseGet(() -> createNew(request, fingerprint));

        boolean deduplicated = alert.getTriggerCount() > 1;
        if (!deduplicated) {
            diagnosisService.diagnoseAsync(alert.getAlertId());
        }

        return new AlertCreateResponse(
                alert.getAlertId(),
                alert.getStatus(),
                alert.getDiagnosisStatus(),
                deduplicated
        );
    }

    public List<AlertRecord> list(String status, String serviceName, String severity,
                                  LocalDateTime startTime, LocalDateTime endTime) {
        return alertRepository.findAll(status, serviceName, severity, startTime, endTime);
    }

    public AlertDetail detail(Long id) {
        AlertRecord alert = findAlert(id);
        return new AlertDetail(alert, historyRepository.findByAlertId(id));
    }

    public AlertRecord updateStatus(Long id, AlertStatusUpdateRequest request) {
        AlertRecord alert = findAlert(id);
        AlertStatus from = alert.getStatus();
        alert.setStatus(request.status());
        AlertRecord saved = alertRepository.save(alert);
        historyRepository.save(id, from, request.status(), request.operator(), request.remark());
        return saved;
    }

    AlertRecord findAlert(Long id) {
        return alertRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("告警不存在"));
    }

    AlertRecord save(AlertRecord alert) {
        return alertRepository.save(alert);
    }

    private AlertRecord createNew(AlertCreateRequest request, String fingerprint) {
        AlertRecord alert = new AlertRecord();
        alert.setServiceName(request.serviceName());
        alert.setAlertType(request.alertType());
        alert.setMetricName(request.metricName());
        alert.setSeverity(request.severity());
        alert.setStatus(AlertStatus.UNHANDLED);
        alert.setFingerprint(fingerprint);
        alert.setTriggerCount(1);
        alert.setMetricsSnapshot(request.metricsSnapshot());
        alert.setLogSnippet(request.logSnippet());
        alert.setDiagnosisStatus(DiagnosisStatus.PENDING);
        AlertRecord saved = alertRepository.save(alert);
        historyRepository.save(saved.getAlertId(), null, AlertStatus.UNHANDLED, "system", "告警首次创建");
        return saved;
    }

    private AlertRecord updateDuplicated(AlertRecord alert, AlertCreateRequest request) {
        alert.setTriggerCount(alert.getTriggerCount() + 1);
        alert.setLastTriggeredAt(LocalDateTime.now());
        alert.setSeverity(request.severity());
        alert.setMetricsSnapshot(request.metricsSnapshot());
        alert.setLogSnippet(request.logSnippet());
        return alertRepository.save(alert);
    }

    private String fingerprint(String serviceName, String alertType, String metricName) {
        return serviceName + ":" + alertType + ":" + metricName;
    }
}

