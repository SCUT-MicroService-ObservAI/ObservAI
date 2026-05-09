package com.observai.notification.repository;

import com.observai.common.enums.NotificationStatus;
import com.observai.notification.model.NotificationRecord;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRecordRepository {
    private final AtomicLong ids = new AtomicLong(1);
    private final CopyOnWriteArrayList<NotificationRecord> records = new CopyOnWriteArrayList<>();

    public NotificationRecord save(NotificationRecord record) {
        record.setId(ids.getAndIncrement());
        record.setCreatedAt(LocalDateTime.now());
        records.add(record);
        return record;
    }

    public List<NotificationRecord> find(Long alertId, String email, NotificationStatus status,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        return records.stream()
                .filter(record -> alertId == null || alertId.equals(record.getAlertId()))
                .filter(record -> email == null || record.getEmail().equalsIgnoreCase(email))
                .filter(record -> status == null || record.getStatus() == status)
                .filter(record -> startTime == null || !record.getCreatedAt().isBefore(startTime))
                .filter(record -> endTime == null || !record.getCreatedAt().isAfter(endTime))
                .sorted(Comparator.comparing(NotificationRecord::getCreatedAt).reversed())
                .toList();
    }
}

