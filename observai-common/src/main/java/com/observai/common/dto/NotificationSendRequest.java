package com.observai.common.dto;

import com.observai.common.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationSendRequest(
        @NotNull Long alertId,
        @NotBlank String serviceName,
        @NotBlank String alertType,
        @NotNull Severity severity,
        String diagnosisSummary,
        String suggestion
) {
}

