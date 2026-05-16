package com.observai.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(scanBasePackages = "com.observai")
public class MonitorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonitorServiceApplication.class, args);
    }
}

