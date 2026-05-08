package com.observai.monitor.scheduler;

import com.observai.monitor.service.MetricCollectorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetricCollectionScheduler {
    private final MetricCollectorService collectorService;

    public MetricCollectionScheduler(MetricCollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @Scheduled(fixedDelayString = "${observai.monitor.collect-interval-ms:15000}")
    public void collect() {
        collectorService.collectAll();
    }
}

