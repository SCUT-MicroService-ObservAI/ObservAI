package com.observai.alert.service;

import com.observai.alert.model.AlertRecord;
import com.observai.alert.repository.AlertRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class AlertServiceAccessor {
    private final AlertRepository repository;

    public AlertServiceAccessor(AlertRepository repository) {
        this.repository = repository;
    }

    AlertRecord find(Long alertId) {
        return repository.findById(alertId).orElseThrow(() -> new IllegalArgumentException("alert not found"));
    }

    AlertRecord save(AlertRecord alert) {
        return repository.save(alert);
    }

    void updateLastNotifiedAt(Long alertId, LocalDateTime at) {
        repository.updateLastNotifiedAt(alertId, at);
    }
}
