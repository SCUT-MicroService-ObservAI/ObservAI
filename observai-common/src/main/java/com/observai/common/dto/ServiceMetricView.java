package com.observai.common.dto;

import com.observai.common.enums.ServiceHealthStatus;
import java.time.LocalDateTime;

public record ServiceMetricView(
        String serviceName,
        double cpu,
        double memory,
        long requestCount,
        double errorRate,
        double responseTime,
        ServiceHealthStatus status,
        LocalDateTime timestamp
) {
}

