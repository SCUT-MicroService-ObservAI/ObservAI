package com.observai.alert.repository;

import com.observai.alert.model.AlertRecord;
import com.observai.common.enums.AlertStatus;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRepository {
    private final AtomicLong ids = new AtomicLong(1000);
    private final ConcurrentHashMap<Long, AlertRecord> alerts = new ConcurrentHashMap<>();

    public Optional<AlertRecord> findOpenByFingerprint(String fingerprint) {
        return alerts.values().stream()
                .filter(alert -> fingerprint.equals(alert.getFingerprint()))
                .filter(alert -> !alert.getStatus().isTerminal())
                .findFirst();
    }

    public Optional<AlertRecord> findById(Long id) {
        return Optional.ofNullable(alerts.get(id));
    }

    public List<AlertRecord> findAll(String status, String serviceName, String severity,
                                     LocalDateTime startTime, LocalDateTime endTime) {
        return alerts.values().stream()
                .filter(alert -> status == null || alert.getStatus().name().equals(status))
                .filter(alert -> serviceName == null || alert.getServiceName().equals(serviceName))
                .filter(alert -> severity == null || alert.getSeverity().name().equals(severity))
                .filter(alert -> startTime == null || !alert.getCreatedAt().isBefore(startTime))
                .filter(alert -> endTime == null || !alert.getCreatedAt().isAfter(endTime))
                .sorted(Comparator.comparing(AlertRecord::getUpdatedAt).reversed())
                .toList();
    }

    public AlertRecord save(AlertRecord alert) {
        if (alert.getAlertId() == null) {
            alert.setAlertId(ids.incrementAndGet());
            alert.setCreatedAt(LocalDateTime.now());
            alert.setFirstTriggeredAt(alert.getCreatedAt());
        }
        alert.setUpdatedAt(LocalDateTime.now());
        if (alert.getLastTriggeredAt() == null) {
            alert.setLastTriggeredAt(alert.getUpdatedAt());
        }
        if (alert.getStatus() == AlertStatus.RECOVERED) {
            alert.setRecoveredAt(LocalDateTime.now());
        }
        alerts.put(alert.getAlertId(), alert);
        return alert;
    }
}

