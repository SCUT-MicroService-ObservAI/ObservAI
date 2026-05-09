package com.observai.common.dto;

import com.observai.common.enums.AlertStatus;
import com.observai.common.enums.DiagnosisStatus;

public record AlertCreateResponse(
        Long alertId,
        AlertStatus status,
        DiagnosisStatus diagnosisStatus,
        boolean deduplicated
) {
}

