package com.observai.notification.service;

import com.observai.common.dto.NotificationSendRequest;
import com.observai.common.enums.NotificationStatus;
import com.observai.notification.model.NotificationConfig;
import com.observai.notification.model.NotificationRecord;
import com.observai.notification.repository.NotificationConfigRepository;
import com.observai.notification.repository.NotificationRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {
    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationConfigRepository configRepository;
    private final NotificationRecordRepository recordRepository;
    private final JavaMailSender mailSender;
    private final String username;
    private final boolean mailConfigured;

    public NotificationDispatchService(NotificationConfigRepository configRepository,
                                       NotificationRecordRepository recordRepository,
                                       ObjectProvider<JavaMailSender> mailSenderProvider,
                                       @Value("${spring.mail.host:}") String mailHost,
                                       @Value("${spring.mail.username:}") String username,
                                       @Value("${spring.mail.password:}") String password) {
        this.configRepository = configRepository;
        this.recordRepository = recordRepository;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.username = username;
        this.mailConfigured = this.mailSender != null && !mailHost.isBlank() && !username.isBlank() && !password.isBlank();
    }

    public void send(NotificationSendRequest request) {
        List<NotificationConfig> configs = configRepository.findEnabled();
        if (configs.isEmpty()) {
            NotificationRecord record = createPendingRecord(request, null);
            complete(record, NotificationStatus.SKIPPED, "no enabled notification config", null);
            return;
        }

        for (NotificationConfig config : configs) {
            NotificationRecord record = createPendingRecord(request, config.getEmail());
            if (!request.severity().atLeast(config.getMinSeverity())) {
                complete(record, NotificationStatus.SKIPPED, "alert severity is below config minimum", null);
                continue;
            }
            if (!mailConfigured) {
                complete(record, NotificationStatus.FAILED, "SMTP configuration is missing", null);
                continue;
            }

            try {
                mailSender.send(buildMessage(record));
                complete(record, NotificationStatus.SUCCESS, null, LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("Failed to send alert notification {} to {}: {}", record.getId(), config.getEmail(), ex.toString(), ex);
                complete(record, NotificationStatus.FAILED, ex.getMessage(), null);
            }
        }
    }

    private NotificationRecord createPendingRecord(NotificationSendRequest request, String email) {
        NotificationRecord record = new NotificationRecord();
        record.setAlertId(request.alertId());
        record.setEmail(email);
        record.setTitle("[" + request.severity() + "] Alert " + request.serviceName() + " " + request.alertType());
        record.setContent("""
                Service: %s
                Alert type: %s
                Severity: %s
                AI diagnosis: %s
                Suggestion: %s
                """.formatted(
                request.serviceName(),
                request.alertType(),
                request.severity(),
                blankToDash(request.diagnosisSummary()),
                blankToDash(request.suggestion())
        ));
        record.setStatus(NotificationStatus.PENDING);
        return recordRepository.save(record);
    }

    private SimpleMailMessage buildMessage(NotificationRecord record) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(record.getEmail());
        message.setSubject(record.getTitle());
        message.setText(record.getContent());
        return message;
    }

    private void complete(NotificationRecord record, NotificationStatus status, String errorMessage, LocalDateTime sentAt) {
        record.setStatus(status);
        record.setErrorMessage(errorMessage);
        record.setSentAt(sentAt);
        recordRepository.updateStatus(record.getId(), status, errorMessage, sentAt);
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
