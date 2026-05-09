package com.observai.notification.controller;

import com.observai.common.api.ApiResponse;
import com.observai.common.dto.NotificationSendRequest;
import com.observai.common.enums.NotificationStatus;
import com.observai.notification.model.NotificationRecord;
import com.observai.notification.repository.NotificationRecordRepository;
import com.observai.notification.service.NotificationDispatchService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {
    private final NotificationDispatchService dispatchService;
    private final NotificationRecordRepository recordRepository;

    public NotificationController(NotificationDispatchService dispatchService, NotificationRecordRepository recordRepository) {
        this.dispatchService = dispatchService;
        this.recordRepository = recordRepository;
    }

    @PostMapping("/notifications/send")
    public ApiResponse<Void> send(@Valid @RequestBody NotificationSendRequest request) {
        dispatchService.send(request);
        return ApiResponse.success();
    }

    @GetMapping("/notification/records")
    public ApiResponse<List<NotificationRecord>> records(
            @RequestParam(required = false) Long alertId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ApiResponse.success(recordRepository.find(alertId, email, status, startTime, endTime));
    }
}

