package com.observai.alert.service;

import com.observai.alert.model.AlertRecord;
import com.observai.alert.repository.AlertRepository;
import org.springframework.stereotype.Component;

@Component
public class AlertServiceAccessor {
    private final AlertRepository repository;

    public AlertServiceAccessor(AlertRepository repository) {
        this.repository = repository;
    }

    AlertRecord find(Long alertId) {
        return repository.findById(alertId).orElseThrow(() -> new IllegalArgumentException("告警不存在"));
    }

    AlertRecord save(AlertRecord alert) {
        return repository.save(alert);
    }
}

