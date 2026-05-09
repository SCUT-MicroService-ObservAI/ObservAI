package com.observai.monitor.repository;

import com.observai.common.enums.Severity;
import com.observai.monitor.model.AlertRule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRuleRepository {
    private final AtomicLong ids = new AtomicLong(1);
    private final ConcurrentHashMap<Long, AlertRule> rules = new ConcurrentHashMap<>();

    public AlertRuleRepository() {
        save(new AlertRule(null, "demo-order-service", "errorRate", ">", 10, 60, Severity.HIGH, true));
        save(new AlertRule(null, "demo-order-service", "cpu", ">", 80, 60, Severity.MEDIUM, true));
        save(new AlertRule(null, "demo-payment-service", "responseTime", ">", 1000, 60, Severity.HIGH, true));
    }

    public List<AlertRule> findAll() {
        return new ArrayList<>(rules.values());
    }

    public List<AlertRule> findEnabledByService(String serviceName) {
        return rules.values().stream()
                .filter(AlertRule::isEnabled)
                .filter(rule -> serviceName.equals(rule.getServiceName()))
                .toList();
    }

    public Optional<AlertRule> findById(Long id) {
        return Optional.ofNullable(rules.get(id));
    }

    public AlertRule save(AlertRule rule) {
        if (rule.getId() == null) {
            rule.setId(ids.getAndIncrement());
            rule.setCreatedAt(LocalDateTime.now());
        }
        rule.setUpdatedAt(LocalDateTime.now());
        rules.put(rule.getId(), rule);
        return rule;
    }

    public void delete(Long id) {
        rules.remove(id);
    }
}

