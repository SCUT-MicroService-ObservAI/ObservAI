package com.observai.notification.controller;

import com.observai.common.api.ApiResponse;
import com.observai.notification.model.ConfigEnabledRequest;
import com.observai.notification.model.NotificationConfig;
import com.observai.notification.service.NotificationConfigService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationConfigController {
    private final NotificationConfigService service;

    public NotificationConfigController(NotificationConfigService service) {
        this.service = service;
    }

    @GetMapping("/notification/configs")
    public ApiResponse<List<NotificationConfig>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping("/notification/configs")
    public ApiResponse<NotificationConfig> create(@RequestBody NotificationConfig config) {
        return ApiResponse.success(service.create(config));
    }

    @PutMapping("/notification/configs/{id}")
    public ApiResponse<NotificationConfig> update(@PathVariable("id") Long id, @RequestBody NotificationConfig config) {
        return ApiResponse.success(service.update(id, config));
    }

    @PutMapping("/notification/configs/{id}/enabled")
    public ApiResponse<NotificationConfig> setEnabled(@PathVariable("id") Long id, @RequestBody ConfigEnabledRequest request) {
        return ApiResponse.success(service.setEnabled(id, request.enabled()));
    }

    @DeleteMapping("/notification/configs/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}
