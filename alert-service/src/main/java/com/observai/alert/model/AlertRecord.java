package com.observai.alert.model;

import com.observai.common.dto.DiagnosisResult;
import com.observai.common.dto.MetricsSnapshot;
import com.observai.common.enums.AlertStatus;
import com.observai.common.enums.DiagnosisStatus;
import com.observai.common.enums.Severity;
import java.time.LocalDateTime;

public class AlertRecord {
    private Long alertId;
    private String serviceName;
    private String alertType;
    private String metricName;
    private Severity severity;
    private AlertStatus status;
    private String fingerprint;
    private int triggerCount;
    private MetricsSnapshot metricsSnapshot;
    private String logSnippet;
    private DiagnosisStatus diagnosisStatus;
    private DiagnosisResult diagnosisResult;
    private LocalDateTime firstTriggeredAt;
    private LocalDateTime lastTriggeredAt;
    /** 最近一次成功尝试发送通知的时间；合并同一未结束指纹时不重复走诊断/通知流程 */
    private LocalDateTime lastNotifiedAt;
    private LocalDateTime recoveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }
    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    public int getTriggerCount() { return triggerCount; }
    public void setTriggerCount(int triggerCount) { this.triggerCount = triggerCount; }
    public MetricsSnapshot getMetricsSnapshot() { return metricsSnapshot; }
    public void setMetricsSnapshot(MetricsSnapshot metricsSnapshot) { this.metricsSnapshot = metricsSnapshot; }
    public String getLogSnippet() { return logSnippet; }
    public void setLogSnippet(String logSnippet) { this.logSnippet = logSnippet; }
    public DiagnosisStatus getDiagnosisStatus() { return diagnosisStatus; }
    public void setDiagnosisStatus(DiagnosisStatus diagnosisStatus) { this.diagnosisStatus = diagnosisStatus; }
    public DiagnosisResult getDiagnosisResult() { return diagnosisResult; }
    public void setDiagnosisResult(DiagnosisResult diagnosisResult) { this.diagnosisResult = diagnosisResult; }
    public LocalDateTime getFirstTriggeredAt() { return firstTriggeredAt; }
    public void setFirstTriggeredAt(LocalDateTime firstTriggeredAt) { this.firstTriggeredAt = firstTriggeredAt; }
    public LocalDateTime getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
    public LocalDateTime getLastNotifiedAt() { return lastNotifiedAt; }
    public void setLastNotifiedAt(LocalDateTime lastNotifiedAt) { this.lastNotifiedAt = lastNotifiedAt; }
    public LocalDateTime getRecoveredAt() { return recoveredAt; }
    public void setRecoveredAt(LocalDateTime recoveredAt) { this.recoveredAt = recoveredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

