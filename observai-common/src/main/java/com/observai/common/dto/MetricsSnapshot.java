package com.observai.common.dto;

public record MetricsSnapshot(
        double cpu,
        double memory,
        long requestCount,
        double errorRate,
        double responseTime
) {
}

