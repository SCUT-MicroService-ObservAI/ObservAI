package com.observai.demo.user;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoUserController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return Map.of(
                "cpu", random.nextDouble(15, 55),
                "memory", random.nextDouble(20, 65),
                "requestCount", random.nextLong(500, 2000),
                "errorRate", random.nextDouble(0, 3),
                "responseTime", random.nextDouble(80, 240)
        );
    }

    @GetMapping("/test/error")
    public String error() {
        return "java.lang.IllegalStateException: user profile cache miss";
    }

    @GetMapping("/test/slow")
    public Map<String, String> slow() throws InterruptedException {
        Thread.sleep(800);
        return Map.of("result", "slow user endpoint finished");
    }
}

