package com.observai.notification.service;

import com.observai.common.dto.NotificationSendRequest;
import com.observai.common.enums.NotificationStatus;
import com.observai.notification.model.NotificationConfig;
import com.observai.notification.model.NotificationRecord;
import com.observai.notification.repository.NotificationConfigRepository;
import com.observai.notification.repository.NotificationRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {
    private final NotificationConfigRepository configRepository;
    private final NotificationRecordRepository recordRepository;
    private final boolean mailConfigured;

    public NotificationDispatchService(NotificationConfigRepository configRepository,
                                       NotificationRecordRepository recordRepository,
                                       @Value("${spring.mail.host:}") String mailHost,
                                       @Value("${spring.mail.username:}") String username,
                                       @Value("${spring.mail.password:}") String password) {
        this.configRepository = configRepository;
        this.recordRepository = recordRepository;
        this.mailConfigured = !mailHost.isBlank() && !username.isBlank() && !password.isBlank();
    }

    public void send(NotificationSendRequest request) {
        List<NotificationConfig> configs = configRepository.findEnabled();
        if (configs.isEmpty()) {
            saveRecord(request, null, NotificationStatus.SKIPPED, "无启用通知配置");
            return;
        }
        for (NotificationConfig config : configs) {
            if (!request.severity().atLeast(config.getMinSeverity())) {
                saveRecord(request, config.getEmail(), NotificationStatus.SKIPPED, "告警级别低于最低通知级别");
                continue;
            }
            if (!mailConfigured) {
                saveRecord(request, config.getEmail(), NotificationStatus.FAILED, "SMTP 配置缺失");
                continue;
            }
            // 框架阶段仅落发送边界和记录，真实 SMTP 发送可在此替换为 JavaMailSender。
            saveRecord(request, config.getEmail(), NotificationStatus.SUCCESS, null);
        }
    }

    private void saveRecord(NotificationSendRequest request, String email, NotificationStatus status, String errorMessage) {
        NotificationRecord record = new NotificationRecord();
        record.setAlertId(request.alertId());
        record.setEmail(email);
        record.setTitle("【" + request.severity() + " 告警】" + request.serviceName() + " " + request.alertType());
        record.setContent("""
                服务名称：%s
                告警类型：%s
                严重等级：%s
                AI 诊断：%s
                处理建议：%s
                """.formatted(request.serviceName(), request.alertType(), request.severity(),
                request.diagnosisSummary(), request.suggestion()));
        record.setStatus(status);
        record.setErrorMessage(errorMessage);
        record.setSentAt(status == NotificationStatus.SUCCESS ? LocalDateTime.now() : null);
        recordRepository.save(record);
    }
}

