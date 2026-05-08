package com.observai.notification.repository;

import com.observai.common.enums.Severity;
import com.observai.notification.model.NotificationConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationConfigRepository {
    private final AtomicLong ids = new AtomicLong(1);
    private final ConcurrentHashMap<Long, NotificationConfig> configs = new ConcurrentHashMap<>();

    public NotificationConfigRepository() {
        NotificationConfig config = new NotificationConfig();
        config.setEmail("ops@example.com");
        config.setMinSeverity(Severity.HIGH);
        config.setEnabled(true);
        save(config);
    }

    public List<NotificationConfig> findAll() {
        return new ArrayList<>(configs.values());
    }

    public List<NotificationConfig> findEnabled() {
        return configs.values().stream().filter(NotificationConfig::isEnabled).toList();
    }

    public Optional<NotificationConfig> findById(Long id) {
        return Optional.ofNullable(configs.get(id));
    }

    public NotificationConfig save(NotificationConfig config) {
        if (config.getId() == null) {
            config.setId(ids.getAndIncrement());
            config.setCreatedAt(LocalDateTime.now());
        }
        config.setUpdatedAt(LocalDateTime.now());
        configs.put(config.getId(), config);
        return config;
    }

    public void delete(Long id) {
        configs.remove(id);
    }
}

