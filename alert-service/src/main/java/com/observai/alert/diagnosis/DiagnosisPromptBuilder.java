package com.observai.alert.diagnosis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observai.alert.model.AlertRecord;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisPromptBuilder {
    private static final String SYSTEM_PROMPT = """
            You are an SRE incident diagnosis assistant.
            Analyze the alert and return exactly one JSON object. Do not use markdown or extra prose.
            Required fields:
            faultType, rootCause, impactScope, suggestionSteps, rollbackSuggestion,
            severity, confidence, needManualHandle.
            impactScope and suggestionSteps must be string arrays.
            severity must be one of LOW, MEDIUM, HIGH, CRITICAL.
            confidence must be a number from 0 to 1.
            needManualHandle must be a boolean.
            """;

    private final ObjectMapper objectMapper;

    public DiagnosisPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String userPrompt(AlertRecord alert) {
        return """
                serviceName: %s
                alertType: %s
                metricName: %s
                severity: %s
                metricsSnapshot: %s
                logSnippet:
                %s
                triggerCount: %d
                lastTriggeredAt: %s
                """.formatted(
                alert.getServiceName(),
                alert.getAlertType(),
                alert.getMetricName(),
                alert.getSeverity(),
                formatMetrics(alert),
                blankToDash(alert.getLogSnippet()),
                alert.getTriggerCount(),
                alert.getLastTriggeredAt() == null ? "-" : alert.getLastTriggeredAt()
        );
    }

    private String formatMetrics(AlertRecord alert) {
        if (alert.getMetricsSnapshot() == null) {
            return "-";
        }
        try {
            return objectMapper.writeValueAsString(alert.getMetricsSnapshot());
        } catch (JsonProcessingException ex) {
            return alert.getMetricsSnapshot().toString();
        }
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
