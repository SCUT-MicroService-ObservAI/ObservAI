package com.observai.monitor.client;

import com.observai.common.api.ApiResponse;
import com.observai.common.dto.AlertCreateRequest;
import com.observai.common.dto.AlertCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "alert-service")
public interface AlertClient {
    @PostMapping("/alerts")
    ApiResponse<AlertCreateResponse> createAlert(@RequestBody AlertCreateRequest request);
}

