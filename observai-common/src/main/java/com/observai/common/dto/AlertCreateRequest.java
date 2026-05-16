package com.observai.common.dto;

import com.observai.common.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlertCreateRequest(
        @NotBlank String serviceName,
        @NotBlank String alertType,
        @NotBlank String metricName,
        @NotNull Severity severity,
        @NotNull MetricsSnapshot metricsSnapshot,
        String logSnippet
) {
}

