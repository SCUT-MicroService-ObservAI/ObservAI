package com.observai.alert.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.observai.alert.model.AlertRecord;
import com.observai.common.dto.DiagnosisResult;
import com.observai.common.dto.MetricsSnapshot;
import com.observai.common.enums.AlertStatus;
import com.observai.common.enums.DiagnosisStatus;
import com.observai.common.enums.Severity;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AlertRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<AlertRecord> findOpenByFingerprint(String fingerprint) {
        try {
            AlertRecord alert = jdbcTemplate.queryForObject(
                    """
                    SELECT *
                    FROM alerts
                    WHERE fingerprint = ?
                      AND status NOT IN ('RESOLVED', 'IGNORED', 'FALSE_ALARM', 'RECOVERED')
                    ORDER BY updated_at DESC
                    LIMIT 1
                    """,
                    this::mapRow,
                    fingerprint
            );
            return Optional.ofNullable(alert);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<AlertRecord> findById(Long id) {
        try {
            AlertRecord alert = jdbcTemplate.queryForObject("SELECT * FROM alerts WHERE alert_id = ?", this::mapRow, id);
            return Optional.ofNullable(alert);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<AlertRecord> findAll(String status, String serviceName, String severity,
                                     LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sql = new StringBuilder("SELECT * FROM alerts WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (status != null) {
            sql.append(" AND status = ?");
            args.add(status);
        }
        if (serviceName != null) {
            sql.append(" AND service_name = ?");
            args.add(serviceName);
        }
        if (severity != null) {
            sql.append(" AND severity = ?");
            args.add(severity);
        }
        if (startTime != null) {
            sql.append(" AND created_at >= ?");
            args.add(startTime);
        }
        if (endTime != null) {
            sql.append(" AND created_at <= ?");
            args.add(endTime);
        }
        sql.append(" ORDER BY updated_at DESC");
        return jdbcTemplate.query(sql.toString(), this::mapRow, args.toArray());
    }

    public AlertRecord save(AlertRecord alert) {
        LocalDateTime now = LocalDateTime.now();
        if (alert.getAlertId() == null) {
            if (alert.getCreatedAt() == null) {
                alert.setCreatedAt(now);
            }
            if (alert.getFirstTriggeredAt() == null) {
                alert.setFirstTriggeredAt(alert.getCreatedAt());
            }
            if (alert.getLastTriggeredAt() == null) {
                alert.setLastTriggeredAt(now);
            }
            alert.setUpdatedAt(now);
            if (alert.getStatus() == AlertStatus.RECOVERED && alert.getRecoveredAt() == null) {
                alert.setRecoveredAt(now);
            }

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        """
                        INSERT INTO alerts
                          (service_name, alert_type, metric_name, severity, status, fingerprint,
                           trigger_count, metrics_snapshot, log_snippet, diagnosis_status, diagnosis_result,
                           first_triggered_at, last_triggered_at, recovered_at, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        Statement.RETURN_GENERATED_KEYS
                );
                bindAlert(ps, alert);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                alert.setAlertId(key.longValue());
            }
            return findById(alert.getAlertId()).orElse(alert);
        }

        alert.setUpdatedAt(now);
        if (alert.getLastTriggeredAt() == null) {
            alert.setLastTriggeredAt(now);
        }
        if (alert.getStatus() == AlertStatus.RECOVERED && alert.getRecoveredAt() == null) {
            alert.setRecoveredAt(now);
        }
        jdbcTemplate.update(
                """
                UPDATE alerts
                SET service_name = ?, alert_type = ?, metric_name = ?, severity = ?, status = ?,
                    fingerprint = ?, trigger_count = ?, metrics_snapshot = ?,
                    log_snippet = ?, diagnosis_status = ?, diagnosis_result = ?,
                    first_triggered_at = ?, last_triggered_at = ?, recovered_at = ?, updated_at = ?
                WHERE alert_id = ?
                """,
                alert.getServiceName(),
                alert.getAlertType(),
                alert.getMetricName(),
                alert.getSeverity().name(),
                alert.getStatus().name(),
                alert.getFingerprint(),
                alert.getTriggerCount(),
                toJson(alert.getMetricsSnapshot()),
                alert.getLogSnippet(),
                alert.getDiagnosisStatus().name(),
                toJson(alert.getDiagnosisResult()),
                alert.getFirstTriggeredAt(),
                alert.getLastTriggeredAt(),
                alert.getRecoveredAt(),
                alert.getUpdatedAt(),
                alert.getAlertId()
        );
        return findById(alert.getAlertId()).orElse(alert);
    }

    private void bindAlert(PreparedStatement ps, AlertRecord alert) throws SQLException {
        ps.setString(1, alert.getServiceName());
        ps.setString(2, alert.getAlertType());
        ps.setString(3, alert.getMetricName());
        ps.setString(4, alert.getSeverity().name());
        ps.setString(5, alert.getStatus().name());
        ps.setString(6, alert.getFingerprint());
        ps.setInt(7, alert.getTriggerCount());
        ps.setString(8, toJson(alert.getMetricsSnapshot()));
        ps.setString(9, alert.getLogSnippet());
        ps.setString(10, alert.getDiagnosisStatus().name());
        ps.setString(11, toJson(alert.getDiagnosisResult()));
        ps.setTimestamp(12, Timestamp.valueOf(alert.getFirstTriggeredAt()));
        ps.setTimestamp(13, Timestamp.valueOf(alert.getLastTriggeredAt()));
        if (alert.getRecoveredAt() == null) {
            ps.setNull(14, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(14, Timestamp.valueOf(alert.getRecoveredAt()));
        }
        ps.setTimestamp(15, Timestamp.valueOf(alert.getCreatedAt()));
        ps.setTimestamp(16, Timestamp.valueOf(alert.getUpdatedAt()));
    }

    private AlertRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        AlertRecord alert = new AlertRecord();
        alert.setAlertId(rs.getLong("alert_id"));
        alert.setServiceName(rs.getString("service_name"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setMetricName(rs.getString("metric_name"));
        alert.setSeverity(Severity.valueOf(rs.getString("severity")));
        alert.setStatus(AlertStatus.valueOf(rs.getString("status")));
        alert.setFingerprint(rs.getString("fingerprint"));
        alert.setTriggerCount(rs.getInt("trigger_count"));
        alert.setMetricsSnapshot(fromJson(rs.getString("metrics_snapshot"), MetricsSnapshot.class));
        alert.setLogSnippet(rs.getString("log_snippet"));
        alert.setDiagnosisStatus(DiagnosisStatus.valueOf(rs.getString("diagnosis_status")));
        alert.setDiagnosisResult(fromJson(rs.getString("diagnosis_result"), DiagnosisResult.class));
        alert.setFirstTriggeredAt(rs.getTimestamp("first_triggered_at").toLocalDateTime());
        alert.setLastTriggeredAt(rs.getTimestamp("last_triggered_at").toLocalDateTime());
        Timestamp recoveredAt = rs.getTimestamp("recovered_at");
        alert.setRecoveredAt(recoveredAt == null ? null : recoveredAt.toLocalDateTime());
        alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        alert.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return alert;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON serialization failed", ex);
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(normalizeDateTimeFields(json), type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("JSON deserialization failed", ex);
        }
    }

    private String normalizeDateTimeFields(String json) {
        return json.replaceAll(
                "(\"diagnosedAt\"\\s*:\\s*\"\\d{4}-\\d{2}-\\d{2}) (\\d{2}:\\d{2}:\\d{2})\"",
                "$1T$2\""
        );
    }
}
