package com.observai.monitor.service;

import com.observai.common.dto.AlertCreateRequest;
import com.observai.common.dto.MetricsSnapshot;
import com.observai.common.dto.ServiceMetricView;
import com.observai.common.enums.ServiceHealthStatus;
import com.observai.common.enums.Severity;
import com.observai.monitor.client.AlertClient;
import com.observai.monitor.model.AlertRule;
import com.observai.monitor.model.DemoHealthResponse;
import com.observai.monitor.repository.AlertRuleRepository;
import com.observai.monitor.repository.ServiceMetricRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MetricCollectorService {
    private static final List<String> MONITORED_SERVICES = List.of(
            "demo-user-service",
            "demo-order-service",
            "demo-payment-service"
    );

    private final RestClient.Builder restClientBuilder;
    private final AlertClient alertClient;
    private final AlertRuleRepository ruleRepository;
    private final ServiceMetricRepository metricRepository;

    public MetricCollectorService(RestClient.Builder restClientBuilder, AlertClient alertClient,
                                  AlertRuleRepository ruleRepository, ServiceMetricRepository metricRepository) {
        this.restClientBuilder = restClientBuilder;
        this.alertClient = alertClient;
        this.ruleRepository = ruleRepository;
        this.metricRepository = metricRepository;
    }

    public void collectAll() {
        MONITORED_SERVICES.forEach(this::collectOne);
    }

    public List<ServiceMetricView> latestServices() {
        return metricRepository.findLatest();
    }

    public List<ServiceMetricView> history(String serviceName, LocalDateTime start, LocalDateTime end) {
        return metricRepository.findHistory(serviceName, start, end);
    }

    private void collectOne(String serviceName) {
        RestClient client = restClientBuilder.baseUrl("http://" + serviceName).build();
        ServiceHealthStatus status = checkHealth(client);
        MetricsSnapshot metrics = fetchMetrics(client);
        ServiceHealthStatus finalStatus = status == ServiceHealthStatus.UP && hasAbnormalMetric(serviceName, metrics)
                ? ServiceHealthStatus.ABNORMAL
                : status;

        ServiceMetricView view = new ServiceMetricView(
                serviceName,
                metrics.cpu(),
                metrics.memory(),
                metrics.requestCount(),
                metrics.errorRate(),
                metrics.responseTime(),
                finalStatus,
                LocalDateTime.now()
        );
        metricRepository.save(view);
        if (status == ServiceHealthStatus.DOWN) {
            alertClient.createAlert(new AlertCreateRequest(
                    serviceName,
                    "SERVICE_DOWN",
                    "status",
                    Severity.CRITICAL,
                    metrics,
                    "health check failed"
            ));
            return;
        }
        evaluateRules(serviceName, metrics, client);
    }

    private ServiceHealthStatus checkHealth(RestClient client) {
        try {
            DemoHealthResponse response = client.get().uri("/health").retrieve().body(DemoHealthResponse.class);
            return response != null && "UP".equalsIgnoreCase(response.status()) ? ServiceHealthStatus.UP : ServiceHealthStatus.DOWN;
        } catch (RuntimeException ex) {
            return ServiceHealthStatus.DOWN;
        }
    }

    private MetricsSnapshot fetchMetrics(RestClient client) {
        try {
            MetricsSnapshot metrics = client.get().uri("/metrics").retrieve().body(MetricsSnapshot.class);
            return metrics == null ? emptyMetrics() : metrics;
        } catch (RuntimeException ex) {
            return emptyMetrics();
        }
    }

    private MetricsSnapshot emptyMetrics() {
        return new MetricsSnapshot(0, 0, 0, 0, 0);
    }

    private boolean hasAbnormalMetric(String serviceName, MetricsSnapshot metrics) {
        return ruleRepository.findEnabledByService(serviceName).stream().anyMatch(rule -> matches(rule, metrics));
    }

    private void evaluateRules(String serviceName, MetricsSnapshot metrics, RestClient client) {
        for (AlertRule rule : ruleRepository.findEnabledByService(serviceName)) {
            if (!matches(rule, metrics)) {
                continue;
            }
            String logSnippet = fetchLogSnippet(client);
            String alertType = toAlertType(rule.getMetricName());
            alertClient.createAlert(new AlertCreateRequest(
                    serviceName,
                    alertType,
                    rule.getMetricName(),
                    rule.getSeverity(),
                    metrics,
                    logSnippet
            ));
        }
    }

    private boolean matches(AlertRule rule, MetricsSnapshot metrics) {
        double actual = metricValue(rule.getMetricName(), metrics);
        return switch (rule.getOperator()) {
            case ">" -> actual > rule.getThreshold();
            case ">=" -> actual >= rule.getThreshold();
            case "<" -> actual < rule.getThreshold();
            case "<=" -> actual <= rule.getThreshold();
            case "==" -> actual == rule.getThreshold();
            default -> false;
        };
    }

    private double metricValue(String metricName, MetricsSnapshot metrics) {
        return switch (metricName) {
            case "cpu" -> metrics.cpu();
            case "memory" -> metrics.memory();
            case "errorRate" -> metrics.errorRate();
            case "responseTime" -> metrics.responseTime();
            case "requestCount" -> metrics.requestCount();
            default -> 0;
        };
    }

    private String fetchLogSnippet(RestClient client) {
        try {
            return client.get().uri("/test/error").retrieve().body(String.class);
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private String toAlertType(String metricName) {
        return switch (metricName) {
            case "errorRate" -> "ERROR_RATE_HIGH";
            case "responseTime" -> "RESPONSE_TIME_HIGH";
            case "cpu" -> "CPU_HIGH";
            case "memory" -> "MEMORY_HIGH";
            default -> "METRIC_ABNORMAL";
        };
    }
}
