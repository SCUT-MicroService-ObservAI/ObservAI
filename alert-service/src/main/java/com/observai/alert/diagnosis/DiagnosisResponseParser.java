package com.observai.alert.diagnosis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observai.alert.model.AlertRecord;
import com.observai.common.dto.DiagnosisResult;
import com.observai.common.enums.Severity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisResponseParser {
    private final ObjectMapper objectMapper;

    public DiagnosisResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DiagnosisResult parse(String raw, AlertRecord alert) {
        try {
            JsonNode node = objectMapper.readTree(stripMarkdown(raw));
            return new DiagnosisResult(
                    text(node, "faultType", defaultFaultType(alert)),
                    text(node, "rootCause", alert.getServiceName() + " is abnormal and needs investigation."),
                    stringList(node.get("impactScope"), List.of(alert.getServiceName())),
                    stringList(node.get("suggestionSteps"), defaultSuggestions()),
                    text(node, "rollbackSuggestion", "If related to a recent release, roll back to the last stable version."),
                    parseSeverity(node.get("severity"), alert.getSeverity()),
                    parseConfidence(node.get("confidence")),
                    parseBoolean(node.get("needManualHandle"), true),
                    LocalDateTime.now(),
                    "ALIYUN_AI"
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse AI diagnosis JSON", ex);
        }
    }

    private String stripMarkdown(String raw) {
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            if (firstLineEnd > 0) {
                text = text.substring(firstLineEnd + 1);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
        }
        return text.trim();
    }

    private String text(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private List<String> stringList(JsonNode node, List<String> defaultValue) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            return defaultValue;
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> {
            String text = item.asText().trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        });
        return values.isEmpty() ? defaultValue : values;
    }

    private Severity parseSeverity(JsonNode node, Severity fallback) {
        if (node == null || node.isNull()) {
            return fallback;
        }
        try {
            return Severity.valueOf(node.asText().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private double parseConfidence(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return 0.75;
        }
        double value = node.asDouble();
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }

    private boolean parseBoolean(JsonNode node, boolean defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        return node.asBoolean(defaultValue);
    }

    private String defaultFaultType(AlertRecord alert) {
        return switch (alert.getAlertType()) {
            case "ERROR_RATE_HIGH" -> "High error rate";
            case "RESPONSE_TIME_HIGH" -> "High response time";
            case "SERVICE_DOWN" -> "Service unavailable";
            default -> "Metric anomaly";
        };
    }

    private List<String> defaultSuggestions() {
        return List.of(
                "Check the latest abnormal log snippets",
                "Verify dependent services and database connectivity",
                "Review recent deployments or configuration changes"
        );
    }
}
