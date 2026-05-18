package com.observai.alert.controller;

import com.observai.alert.model.AlertDetail;
import com.observai.alert.model.AlertRecord;
import com.observai.alert.service.AlertService;
import com.observai.common.api.ApiResponse;
import com.observai.common.dto.AlertCreateRequest;
import com.observai.common.dto.AlertCreateResponse;
import com.observai.common.dto.AlertStatusUpdateRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/alerts")
    public ApiResponse<AlertCreateResponse> create(@Valid @RequestBody AlertCreateRequest request) {
        return ApiResponse.success(alertService.createOrUpdate(request));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<AlertRecord>> list(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @RequestParam(name = "severity", required = false) String severity,
            @RequestParam(name = "startTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ApiResponse.success(alertService.list(status, serviceName, severity, startTime, endTime));
    }

    @GetMapping("/alerts/{id}")
    public ApiResponse<AlertDetail> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(alertService.detail(id));
    }

    @PutMapping("/alerts/{id}/status")
    public ApiResponse<AlertRecord> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody AlertStatusUpdateRequest request) {
        return ApiResponse.success(alertService.updateStatus(id, request));
    }

    @PostMapping("/alerts/{id}/recover")
    public ApiResponse<AlertRecord> recover(@PathVariable("id") Long id) {
        return ApiResponse.success(alertService.markRecovered(id));
    }

    @PostMapping("/alerts/{id}/rediagnose")
    public ApiResponse<Void> rediagnose(@PathVariable("id") Long id) {
        alertService.rediagnose(id);
        return ApiResponse.success();
    }
}
