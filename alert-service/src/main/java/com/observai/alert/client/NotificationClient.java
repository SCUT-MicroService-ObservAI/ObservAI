package com.observai.alert.client;

import com.observai.common.api.ApiResponse;
import com.observai.common.dto.NotificationSendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/notifications/send")
    ApiResponse<Void> send(@RequestBody NotificationSendRequest request);
}

