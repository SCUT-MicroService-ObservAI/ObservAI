package com.observai.common.dto;

import com.observai.common.enums.Severity;
import java.time.LocalDateTime;
import java.util.List;

public record DiagnosisResult(
        String faultType,
        String rootCause,
        List<String> impactScope,
        List<String> suggestionSteps,
        String rollbackSuggestion,
        Severity severity,
        double confidence,
        boolean needManualHandle,
        LocalDateTime diagnosedAt,
        String source
) {
}

