package com.observai.alert.model;

import com.observai.common.enums.AlertStatus;
import java.time.LocalDateTime;

public record AlertStatusHistory(
        Long id,
        Long alertId,
        AlertStatus fromStatus,
        AlertStatus toStatus,
        String operator,
        String remark,
        LocalDateTime createdAt
) {
}

