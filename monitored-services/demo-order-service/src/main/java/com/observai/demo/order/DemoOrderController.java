package com.observai.demo.order;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoOrderController {
    private volatile boolean errorMode;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return Map.of(
                "cpu", random.nextDouble(45, errorMode ? 92 : 75),
                "memory", random.nextDouble(45, 78),
                "requestCount", random.nextLong(1000, 5000),
                "errorRate", errorMode ? random.nextDouble(12, 20) : random.nextDouble(2, 8),
                "responseTime", random.nextDouble(180, errorMode ? 680 : 420)
        );
    }

    @GetMapping("/test/error")
    public String error() {
        errorMode = true;
        return "java.lang.RuntimeException: order create failed, database connection timeout";
    }

    @GetMapping("/test/slow")
    public Map<String, String> slow() throws InterruptedException {
        Thread.sleep(1500);
        return Map.of("result", "slow order endpoint finished");
    }
}

