package com.observai.demo.payment;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoPaymentController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return Map.of(
                "cpu", random.nextDouble(25, 70),
                "memory", random.nextDouble(35, 72),
                "requestCount", random.nextLong(800, 3600),
                "errorRate", random.nextDouble(1, 5),
                "responseTime", random.nextDouble(300, 1200)
        );
    }

    @GetMapping("/test/error")
    public String error() {
        return "java.net.SocketTimeoutException: payment provider response timeout";
    }

    @GetMapping("/test/slow")
    public Map<String, String> slow() throws InterruptedException {
        Thread.sleep(1800);
        return Map.of("result", "slow payment endpoint finished");
    }
}

