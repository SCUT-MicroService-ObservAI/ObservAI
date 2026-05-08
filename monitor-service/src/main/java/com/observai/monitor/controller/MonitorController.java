package com.observai.monitor.controller;

import com.observai.common.api.ApiResponse;
import com.observai.common.dto.ServiceMetricView;
import com.observai.monitor.service.MetricCollectorService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MonitorController {
    private final MetricCollectorService collectorService;

    public MonitorController(MetricCollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @GetMapping("/monitor/services")
    public ApiResponse<List<ServiceMetricView>> services() {
        return ApiResponse.success(collectorService.latestServices());
    }

    @GetMapping("/monitor/services/{serviceName}/metrics")
    public ApiResponse<List<ServiceMetricView>> history(
            @PathVariable String serviceName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ApiResponse.success(collectorService.history(serviceName, startTime, endTime));
    }

    @PostMapping("/monitor/collect")
    public ApiResponse<Void> collectNow() {
        collectorService.collectAll();
        return ApiResponse.success();
    }
}

