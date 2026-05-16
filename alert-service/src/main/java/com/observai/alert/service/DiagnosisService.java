package com.observai.alert.service;

import com.observai.alert.client.AliyunAiClient;
import com.observai.alert.client.NotificationClient;
import com.observai.alert.diagnosis.DiagnosisPromptBuilder;
import com.observai.alert.diagnosis.DiagnosisResponseParser;
import com.observai.alert.model.AlertRecord;
import com.observai.common.dto.DiagnosisResult;
import com.observai.common.dto.NotificationSendRequest;
import com.observai.common.enums.DiagnosisStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DiagnosisService {
    private static final Logger log = LoggerFactory.getLogger(DiagnosisService.class);

    private final AlertServiceAccessor alertServiceAccessor;
    private final NotificationClient notificationClient;
    private final AliyunAiClient aliyunAiClient;
    private final DiagnosisPromptBuilder promptBuilder;
    private final DiagnosisResponseParser responseParser;

    public DiagnosisService(AlertServiceAccessor alertServiceAccessor,
                            NotificationClient notificationClient,
                            AliyunAiClient aliyunAiClient,
                            DiagnosisPromptBuilder promptBuilder,
                            DiagnosisResponseParser responseParser) {
        this.alertServiceAccessor = alertServiceAccessor;
        this.notificationClient = notificationClient;
        this.aliyunAiClient = aliyunAiClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
    }

    /**
     * 新告警创建后异步诊断，不阻塞上报接口。
     */
    @Async("diagnosisExecutor")
    public void diagnoseAsync(Long alertId) {
        runDiagnosis(alertId);
    }

    /**
     * 同步执行诊断（用于「重新诊断」接口，避免客户端已返回但异步尚未落库）。
     */
    public void runDiagnosis(Long alertId) {
        AlertRecord alert = alertServiceAccessor.find(alertId);
        alert.setDiagnosisStatus(DiagnosisStatus.RUNNING);
        alertServiceAccessor.save(alert);

        DiagnosisResult result;
        DiagnosisStatus status;
        try {
            String raw = aliyunAiClient.chat(promptBuilder.systemPrompt(), promptBuilder.userPrompt(alert));
            result = responseParser.parse(raw, alert);
            status = DiagnosisStatus.SUCCESS;
        } catch (Exception ex) {
            log.warn("AI diagnosis failed for alert {}: {}", alertId, ex.toString(), ex);
            result = mockDiagnosis(alert);
            status = DiagnosisStatus.MOCKED;
        }

        alert.setDiagnosisResult(result);
        alert.setDiagnosisStatus(status);
        alertServiceAccessor.save(alert);

        try {
            notificationClient.send(new NotificationSendRequest(
                    alert.getAlertId(),
                    alert.getServiceName(),
                    alert.getAlertType(),
                    alert.getSeverity(),
                    result.rootCause(),
                    String.join("；", result.suggestionSteps()),
                    alert.getStatus(),
                    alert.getLastTriggeredAt()
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
