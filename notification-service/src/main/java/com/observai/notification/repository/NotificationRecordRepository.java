package com.observai.notification.repository;

import com.observai.common.enums.NotificationStatus;
import com.observai.notification.model.NotificationRecord;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRecordRepository {
    private final JdbcTemplate jdbcTemplate;

    public NotificationRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public NotificationRecord save(NotificationRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO notification_record
                      (alert_id, email, title, content, status, error_message, sent_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            if (record.getAlertId() == null) {
                ps.setObject(1, null);
            } else {
                ps.setLong(1, record.getAlertId());
            }
            ps.setString(2, record.getEmail());
            ps.setString(3, record.getTitle());
            ps.setString(4, record.getContent());
            ps.setString(5, record.getStatus().name());
            ps.setString(6, record.getErrorMessage());
            if (record.getSentAt() == null) {
                ps.setTimestamp(7, null);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(record.getSentAt()));
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            record.setId(key.longValue());
        }
        return record;
    }

    public void updateStatus(Long id, NotificationStatus status, String errorMessage, LocalDateTime sentAt) {
        jdbcTemplate.update(
                """
                UPDATE notification_record
                SET status = ?, error_message = ?, sent_at = ?
                WHERE id = ?
                """,
                status.name(),
                errorMessage,
                sentAt == null ? null : Timestamp.valueOf(sentAt),
                id
        );
    }

    public List<NotificationRecord> find(Long alertId, String email, NotificationStatus status,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT id, alert_id, email, title, content, status, error_message, sent_at, created_at
                FROM notification_record
                WHERE 1 = 1
                """
        );
        List<Object> args = new ArrayList<>();
        if (alertId != null) {
            sql.append(" AND alert_id = ?");
            args.add(alertId);
        }
        if (email != null) {
            sql.append(" AND email = ?");
            args.add(email);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            args.add(status.name());
        }
        if (startTime != null) {
            sql.append(" AND created_at >= ?");
            args.add(startTime);
        }
        if (endTime != null) {
            sql.append(" AND created_at <= ?");
            args.add(endTime);
        }
        sql.append(" ORDER BY created_at DESC");
        return jdbcTemplate.query(sql.toString(), this::mapRow, args.toArray());
    }

    private NotificationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        NotificationRecord record = new NotificationRecord();
        record.setId(rs.getLong("id"));
        long alertId = rs.getLong("alert_id");
        record.setAlertId(rs.wasNull() ? null : alertId);
        record.setEmail(rs.getString("email"));
        record.setTitle(rs.getString("title"));
        record.setContent(rs.getString("content"));
        record.setStatus(NotificationStatus.valueOf(rs.getString("status")));
        record.setErrorMessage(rs.getString("error_message"));
        Timestamp sentAt = rs.getTimestamp("sent_at");
        record.setSentAt(sentAt == null ? null : sentAt.toLocalDateTime());
        record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return record;
    }
}

