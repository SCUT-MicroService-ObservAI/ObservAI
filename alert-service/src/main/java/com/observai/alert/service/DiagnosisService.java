package com.observai.alert.service;

import com.observai.alert.client.AliyunAiClient;
import com.observai.alert.client.NotificationClient;
import com.observai.alert.diagnosis.DiagnosisPromptBuilder;
import com.observai.alert.diagnosis.DiagnosisResponseParser;
import com.observai.alert.model.AlertRecord;
import com.observai.common.dto.DiagnosisResult;
import com.observai.common.dto.NotificationSendRequest;
import com.observai.common.enums.DiagnosisStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {
    private static final Logger log = LoggerFactory.getLogger(DiagnosisService.class);

    private final AlertServiceAccessor alertServiceAccessor;
    private final NotificationClient notificationClient;
    private final AliyunAiClient aliyunAiClient;
    private final DiagnosisPromptBuilder promptBuilder;
    private final DiagnosisResponseParser responseParser;

    public DiagnosisService(AlertServiceAccessor alertServiceAccessor,
                            NotificationClient notificationClient,
                            AliyunAiClient aliyunAiClient,
                            DiagnosisPromptBuilder promptBuilder,
                            DiagnosisResponseParser responseParser) {
        this.alertServiceAccessor = alertServiceAccessor;
        this.notificationClient = notificationClient;
        this.aliyunAiClient = aliyunAiClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
    }

    @Async("diagnosisExecutor")
    public void diagnoseAsync(Long alertId) {
        runDiagnosis(alertId);
    }

    public void runDiagnosis(Long alertId) {
        AlertRecord alert = alertServiceAccessor.find(alertId);
        alert.setDiagnosisStatus(DiagnosisStatus.RUNNING);
        alertServiceAccessor.save(alert);

        DiagnosisResult result;
        DiagnosisStatus status;
        try {
            String raw = aliyunAiClient.chat(promptBuilder.systemPrompt(), promptBuilder.userPrompt(alert));
            result = responseParser.parse(raw, alert);
            status = DiagnosisStatus.SUCCESS;
        } catch (Exception ex) {
            log.warn("AI diagnosis failed for alert {}: {}", alertId, ex.toString(), ex);
            result = mockDiagnosis(alert);
            status = DiagnosisStatus.MOCKED;
        }

        alert.setDiagnosisResult(result);
        alert.setDiagnosisStatus(status);
        alertServiceAccessor.save(alert);

        try {
            notificationClient.send(new NotificationSendRequest(
                    alert.getAlertId(),
                    alert.getServiceName(),
                    alert.getAlertType(),
                    alert.getSeverity(),
                    result.rootCause(),
                    String.join("; ", result.suggestionSteps())
            ));
            alertServiceAccessor.updateLastNotifiedAt(alert.getAlertId(), LocalDateTime.now());
        } catch (RuntimeException ex) {
            log.warn("Notification dispatch failed for alert {}: {}", alertId, ex.toString());
        }
    }

    private DiagnosisResult mockDiagnosis(AlertRecord alert) {
        return new DiagnosisResult(
                switch (alert.getAlertType()) {
                    case "ERROR_RATE_HIGH" -> "High error rate";
                    case "RESPONSE_TIME_HIGH" -> "High response time";
                    case "SERVICE_DOWN" -> "Service unavailable";
                    default -> "Metric anomaly";
                },
                alert.getServiceName() + " metric " + alert.getMetricName()
                        + " crossed the threshold. Check logs, dependencies, and recent changes.",
                List.of(alert.getServiceName()),
                List.of(
                        "Check the latest abnormal log snippets",
                        "Verify dependent services and database connectivity",
                        "Review recent deployments or configuration changes"
                ),
                "If the anomaly correlates with a recent release, roll back to the last stable version.",
                alert.getSeverity(),
                0.72,
                true,
                LocalDateTime.now(),
                "MOCK"
        );
    }
}
