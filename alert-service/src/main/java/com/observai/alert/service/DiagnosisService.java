package com.observai.alert.service;

import com.observai.alert.client.NotificationClient;
import com.observai.alert.model.AlertRecord;
import com.observai.common.dto.DiagnosisResult;
import com.observai.common.dto.NotificationSendRequest;
import com.observai.common.enums.DiagnosisStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {
    private final AlertServiceAccessor alertServiceAccessor;
    private final NotificationClient notificationClient;

    public DiagnosisService(AlertServiceAccessor alertServiceAccessor, NotificationClient notificationClient) {
        this.alertServiceAccessor = alertServiceAccessor;
        this.notificationClient = notificationClient;
    }

    @Async("diagnosisExecutor")
    public void diagnoseAsync(Long alertId) {
        AlertRecord alert = alertServiceAccessor.find(alertId);
        alert.setDiagnosisStatus(DiagnosisStatus.RUNNING);
        alertServiceAccessor.save(alert);

        DiagnosisResult result = mockDiagnosis(alert);
        alert.setDiagnosisResult(result);
        alert.setDiagnosisStatus(DiagnosisStatus.MOCKED);
        alertServiceAccessor.save(alert);

        try {
            notificationClient.send(new NotificationSendRequest(
                    alert.getAlertId(),
                    alert.getServiceName(),
                    alert.getAlertType(),
                    alert.getSeverity(),
                    result.rootCause(),
                    String.join("；", result.suggestionSteps())
            ));
            alertServiceAccessor.updateLastNotifiedAt(alert.getAlertId(), LocalDateTime.now());
        } catch (RuntimeException ignored) {
            // 通知失败不能影响告警与诊断主流程。
        }
    }

    private DiagnosisResult mockDiagnosis(AlertRecord alert) {
        return new DiagnosisResult(
                switch (alert.getAlertType()) {
                    case "ERROR_RATE_HIGH" -> "接口错误率过高";
                    case "RESPONSE_TIME_HIGH" -> "接口响应时间过高";
                    case "SERVICE_DOWN" -> "服务不可用";
                    default -> "指标异常";
                },
                alert.getServiceName() + " 指标 " + alert.getMetricName() + " 触发阈值，需结合日志和近期变更排查。",
                List.of(alert.getServiceName()),
                List.of("检查异常日志片段", "确认依赖服务和数据库连接", "核对最近发布或配置变更"),
                "若异常与最近发布强相关，建议回滚到上一稳定版本。",
                alert.getSeverity(),
                0.72,
                true,
                LocalDateTime.now(),
                "MOCK"
        );
    }
}

