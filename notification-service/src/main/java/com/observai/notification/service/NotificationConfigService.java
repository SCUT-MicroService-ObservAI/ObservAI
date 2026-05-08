package com.observai.notification.service;

import com.observai.notification.model.NotificationConfig;
import com.observai.notification.repository.NotificationConfigRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationConfigService {
    private final NotificationConfigRepository repository;

    public NotificationConfigService(NotificationConfigRepository repository) {
        this.repository = repository;
    }

    public List<NotificationConfig> list() {
        return repository.findAll();
    }

    public NotificationConfig create(NotificationConfig config) {
        config.setId(null);
        return repository.save(config);
    }

    public NotificationConfig update(Long id, NotificationConfig input) {
        NotificationConfig config = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("通知配置不存在"));
        config.setEmail(input.getEmail());
        config.setMinSeverity(input.getMinSeverity());
        config.setEnabled(input.isEnabled());
        return repository.save(config);
    }

    public NotificationConfig setEnabled(Long id, boolean enabled) {
        NotificationConfig config = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("通知配置不存在"));
        config.setEnabled(enabled);
        return repository.save(config);
    }

    public void delete(Long id) {
        repository.delete(id);
    }
}

