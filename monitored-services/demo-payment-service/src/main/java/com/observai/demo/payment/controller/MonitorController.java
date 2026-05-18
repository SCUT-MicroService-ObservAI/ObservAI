package com.observai.demo.payment.controller;

import com.observai.demo.payment.model.HealthResponse;
import com.observai.demo.payment.model.MetricsResponse;
import com.observai.demo.payment.model.SlowResponse;
import com.observai.demo.payment.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class MonitorController {
    private static final Logger log = LoggerFactory.getLogger(MonitorController.class);
    private final MetricsService metricsService;

    public MonitorController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP");
    }

    /**
     * 运行指标接口
     */
    @GetMapping("/metrics")
    public MetricsResponse metrics() {
        MetricsResponse metrics = metricsService.generateMetrics();
        log.debug("Generated metrics: cpu={}, memory={}, errorRate={}, responseTime={}",
                metrics.getCpu(), metrics.getMemory(), metrics.getErrorRate(), metrics.getResponseTime());
        return metrics;
    }

    /**
     * 模拟异常日志接口
     */
    @GetMapping(value = "/test/error", produces = MediaType.TEXT_PLAIN_VALUE)
    public String testError() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String errorLog = String.format(
                "%s ERROR [demo-payment-service] [http-nio-8093-exec-1] c.o.d.p.service.PaymentService : 支付服务异常\n" +
                "java.lang.RuntimeException: Payment processing failed - third party payment gateway timeout\n" +
                "\tat com.observai.demo.payment.service.PaymentService.processPayment(PaymentService.java:67)\n" +
                "\tat com.observai.demo.payment.controller.PaymentController.pay(PaymentController.java:42)\n" +
                "\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
                "\tat java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)\n" +
                "Caused by: java.net.SocketTimeoutException: Read timed out\n" +
                "\tat java.base/java.net.SocketInputStream.socketRead0(Native Method)\n" +
                "\tat java.base/java.net.SocketInputStream.socketRead(SocketInputStream.java:115)\n" +
                "\tat com.observai.demo.payment.client.PaymentGatewayClient.call(PaymentGatewayClient.java:156)\n" +
                "\t... 22 more\n",
                timestamp
        );
        
        log.error("Simulated error log requested");
        return errorLog;
    }

    /**
     * 模拟慢接口
     */
    @GetMapping("/test/slow")
    public SlowResponse testSlow() throws InterruptedException {
        long delay = 800 + (long) (Math.random() * 400); // 800-1200ms
        log.warn("Slow endpoint called, delaying {}ms", delay);
        Thread.sleep(delay);
        return new SlowResponse("completed", delay);
    }
}
