package com.observai.notification.service;

import com.observai.common.dto.NotificationSendRequest;
import com.observai.common.enums.NotificationStatus;
import com.observai.notification.model.NotificationConfig;
import com.observai.notification.model.NotificationRecord;
import com.observai.notification.repository.NotificationConfigRepository;
import com.observai.notification.repository.NotificationRecordRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationConfigRepository configRepository;
    private final NotificationRecordRepository recordRepository;
    private final JavaMailSender mailSender;
    private final boolean mailConfigured;

    public NotificationDispatchService(NotificationConfigRepository configRepository,
                                       NotificationRecordRepository recordRepository,
                                       JavaMailSender mailSender,
                                       @Value("${spring.mail.host:}") String mailHost,
                                       @Value("${spring.mail.username:}") String username,
                                       @Value("${spring.mail.password:}") String password) {
        this.configRepository = configRepository;
        this.recordRepository = recordRepository;
        this.mailSender = mailSender;
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
            try {
                sendMail(config.getEmail(), request);
                saveRecord(request, config.getEmail(), NotificationStatus.SUCCESS, null);
            } catch (Exception ex) {
                log.warn("邮件发送失败 {}: {}", config.getEmail(), ex.toString());
                saveRecord(request, config.getEmail(), NotificationStatus.FAILED, ex.getMessage());
            }
        }
    }

    private void sendMail(String to, NotificationSendRequest request) throws MessagingException, MailException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        helper.setTo(to);
        helper.setSubject("【" + request.severity() + " 告警】" + request.serviceName() + " " + request.alertType());
        helper.setText(buildBody(request));
        mailSender.send(mimeMessage);
    }

    private String buildBody(NotificationSendRequest request) {
        return """
                服务名称：%s
                告警类型：%s
                严重等级：%s
                当前状态：%s
                AI 诊断：%s
                处理建议：%s
                触发时间：%s
                """.formatted(
                request.serviceName(),
                request.alertType(),
                request.severity(),
                request.currentStatus() == null ? "-" : request.currentStatus().name(),
                request.diagnosisSummary() == null ? "-" : request.diagnosisSummary(),
                request.suggestion() == null ? "-" : request.suggestion(),
                request.triggeredAt() == null ? "-" : request.triggeredAt()
        );
    }

    private void saveRecord(NotificationSendRequest request, String email, NotificationStatus status, String errorMessage) {
        NotificationRecord record = new NotificationRecord();
        record.setAlertId(request.alertId());
        record.setEmail(email);
        record.setTitle("【" + request.severity() + " 告警】" + request.serviceName() + " " + request.alertType());
        record.setContent(buildBody(request));
        record.setStatus(status);
        record.setErrorMessage(errorMessage);
        record.setSentAt(status == NotificationStatus.SUCCESS ? LocalDateTime.now() : null);
        recordRepository.save(record);
    }
}