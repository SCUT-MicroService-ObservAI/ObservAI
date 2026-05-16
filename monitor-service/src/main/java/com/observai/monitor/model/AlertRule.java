package com.observai.monitor.model;

import com.observai.common.enums.Severity;
import java.time.LocalDateTime;

public class AlertRule {
    private Long id;
    private String serviceName;
    private String metricName;
    private String operator;
    private double threshold;
    private int durationSeconds;
    private Severity severity;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AlertRule() {
    }

    public AlertRule(Long id, String serviceName, String metricName, String operator, double threshold,
                     int durationSeconds, Severity severity, boolean enabled) {
        this.id = id;
        this.serviceName = serviceName;
        this.metricName = metricName;
        this.operator = operator;
        this.threshold = threshold;
        this.durationSeconds = durationSeconds;
        this.severity = severity;
        this.enabled = enabled;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

