package com.observai.alert.repository;

import com.observai.alert.model.AlertStatusHistory;
import com.observai.common.enums.AlertStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class AlertStatusHistoryRepository {
    private final AtomicLong ids = new AtomicLong(1);
    private final CopyOnWriteArrayList<AlertStatusHistory> history = new CopyOnWriteArrayList<>();

    public void save(Long alertId, AlertStatus fromStatus, AlertStatus toStatus, String operator, String remark) {
        history.add(new AlertStatusHistory(
                ids.getAndIncrement(),
                alertId,
                fromStatus,
                toStatus,
                operator == null ? "system" : operator,
                remark,
                LocalDateTime.now()
        ));
    }

    public List<AlertStatusHistory> findByAlertId(Long alertId) {
        return new ArrayList<>(history).stream()
                .filter(item -> alertId.equals(item.alertId()))
                .sorted(Comparator.comparing(AlertStatusHistory::createdAt))
                .toList();
    }
}

