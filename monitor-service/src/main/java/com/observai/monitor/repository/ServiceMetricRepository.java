package com.observai.monitor.repository;

import com.observai.common.dto.ServiceMetricView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceMetricRepository {
    private final ConcurrentHashMap<String, ServiceMetricView> latest = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ServiceMetricView>> history = new ConcurrentHashMap<>();

    public void save(ServiceMetricView metric) {
        latest.put(metric.serviceName(), metric);
        history.computeIfAbsent(metric.serviceName(), ignored -> new ArrayList<>()).add(metric);
    }

    public List<ServiceMetricView> findLatest() {
        return latest.values().stream()
                .sorted(Comparator.comparing(ServiceMetricView::serviceName))
                .toList();
    }

    public List<ServiceMetricView> findHistory(String serviceName, LocalDateTime start, LocalDateTime end) {
        return history.getOrDefault(serviceName, List.of()).stream()
                .filter(metric -> start == null || !metric.timestamp().isBefore(start))
                .filter(metric -> end == null || !metric.timestamp().isAfter(end))
                .toList();
    }

    public Optional<ServiceMetricView> latestOf(String serviceName) {
        return Optional.ofNullable(latest.get(serviceName));
    }
}

