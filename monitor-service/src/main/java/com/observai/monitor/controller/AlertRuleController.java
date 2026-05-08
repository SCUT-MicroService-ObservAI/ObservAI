package com.observai.monitor.controller;

import com.observai.common.api.ApiResponse;
import com.observai.monitor.model.AlertRule;
import com.observai.monitor.model.RuleEnabledRequest;
import com.observai.monitor.service.AlertRuleService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlertRuleController {
    private final AlertRuleService service;

    public AlertRuleController(AlertRuleService service) {
        this.service = service;
    }

    @GetMapping("/alert-rules")
    public ApiResponse<List<AlertRule>> list() {
        return ApiResponse.success(service.list());
    }

    @PostMapping("/alert-rules")
    public ApiResponse<AlertRule> create(@RequestBody AlertRule rule) {
        return ApiResponse.success(service.create(rule));
    }

    @PutMapping("/alert-rules/{id}")
    public ApiResponse<AlertRule> update(@PathVariable Long id, @RequestBody AlertRule rule) {
        return ApiResponse.success(service.update(id, rule));
    }

    @PutMapping("/alert-rules/{id}/enabled")
    public ApiResponse<AlertRule> setEnabled(@PathVariable Long id, @RequestBody RuleEnabledRequest request) {
        return ApiResponse.success(service.setEnabled(id, request.enabled()));
    }

    @DeleteMapping("/alert-rules/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success();
    }
}

