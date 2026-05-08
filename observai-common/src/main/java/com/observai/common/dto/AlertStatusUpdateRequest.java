package com.observai.common.dto;

import com.observai.common.enums.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record AlertStatusUpdateRequest(
        @NotNull AlertStatus status,
        String remark,
        String operator
) {
}

