package com.observai.alert.diagnosis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observai.alert.model.AlertRecord;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisPromptBuilder {
    private static final String SYSTEM_PROMPT = """
            你是运维故障诊断助手。根据告警信息分析根因并给出可执行建议。
            只输出一个 JSON 对象，不要 markdown，不要额外说明。
            字段名必须为：
            faultType, rootCause, impactScope, suggestionSteps, rollbackSuggestion,
            severity, confidence, needManualHandle
            其中 impactScope 和 suggestionSteps 为字符串数组；
            severity 只能是 LOW、MEDIUM、HIGH、CRITICAL 之一；
            confidence 为 0 到 1 的小数；needManualHandle 为布尔值。
            """;

    private final ObjectMapper objectMapper;

    public DiagnosisPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String userPrompt(AlertRecord alert) {
        String metrics = formatMetrics(alert);
        return """
                服务名称：%s
                告警类型：%s
                指标名称：%s
                严重等级：%s
                指标快照：%s
                异常日志：
                %s
                最近触发次数：%d
                最近触发时间：%s
                """.formatted(
                alert.getServiceName(),
                alert.getAlertType(),
                alert.getMetricName(),
                alert.getSeverity(),
                metrics,
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
