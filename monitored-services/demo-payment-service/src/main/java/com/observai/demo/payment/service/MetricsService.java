package com.observai.demo.payment.service;

import com.observai.demo.payment.model.MetricsResponse;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MetricsService {
    private final Random random = new Random();

    /**
     * 生成动态的运行指标
     * CPU: 15% - 85%
     * Memory: 20% - 80%
     * RequestCount: 500 - 2000
     * ErrorRate: 0% - 15%
     * ResponseTime: 80ms - 600ms
     */
    public MetricsResponse generateMetrics() {
        double cpu = 15 + random.nextDouble() * 70;
        double memory = 20 + random.nextDouble() * 60;
        int requestCount = 500 + random.nextInt(1501);
        double errorRate = random.nextDouble() * 15;
        double responseTime = 80 + random.nextDouble() * 520;

        return new MetricsResponse(
                Math.round(cpu * 10.0) / 10.0,
                Math.round(memory * 10.0) / 10.0,
                requestCount,
                Math.round(errorRate * 10.0) / 10.0,
                Math.round(responseTime * 10.0) / 10.0
        );
    }
}
