package com.observai.monitor.service;

import com.observai.monitor.model.AlertRule;
import com.observai.monitor.repository.AlertRuleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AlertRuleService {
    private final AlertRuleRepository repository;

    public AlertRuleService(AlertRuleRepository repository) {
        this.repository = repository;
    }

    public List<AlertRule> list() {
        return repository.findAll();
    }

    public AlertRule create(AlertRule rule) {
        rule.setId(null);
        return repository.save(rule);
    }

    public AlertRule update(Long id, AlertRule input) {
        AlertRule rule = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("告警规则不存在"));
        rule.setServiceName(input.getServiceName());
        rule.setMetricName(input.getMetricName());
        rule.setOperator(input.getOperator());
        rule.setThreshold(input.getThreshold());
        rule.setDurationSeconds(input.getDurationSeconds());
        rule.setSeverity(input.getSeverity());
        rule.setEnabled(input.isEnabled());
        return repository.save(rule);
    }

    public AlertRule setEnabled(Long id, boolean enabled) {
        AlertRule rule = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("告警规则不存在"));
        rule.setEnabled(enabled);
        return repository.save(rule);
    }

    public void delete(Long id) {
        repository.delete(id);
    }
}

