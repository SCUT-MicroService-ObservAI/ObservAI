package com.observai.alert.repository;

import com.observai.alert.model.AlertStatusHistory;
import com.observai.common.enums.AlertStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AlertStatusHistoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public AlertStatusHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Long alertId, AlertStatus fromStatus, AlertStatus toStatus, String operator, String remark) {
        jdbcTemplate.update(
                """
                INSERT INTO alert_status_history
                  (alert_id, from_status, to_status, operator, remark)
                VALUES (?, ?, ?, ?, ?)
                """,
                alertId,
                fromStatus == null ? null : fromStatus.name(),
                toStatus.name(),
                operator == null ? "system" : operator,
                remark
        );
    }

    public List<AlertStatusHistory> findByAlertId(Long alertId) {
        return jdbcTemplate.query(
                """
                SELECT id, alert_id, from_status, to_status, operator, remark, created_at
                FROM alert_status_history
                WHERE alert_id = ?
                ORDER BY created_at
                """,
                this::mapRow,
                alertId
        );
    }

    private AlertStatusHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
        String fromStatus = rs.getString("from_status");
        return new AlertStatusHistory(
                rs.getLong("id"),
                rs.getLong("alert_id"),
                fromStatus == null ? null : AlertStatus.valueOf(fromStatus),
                AlertStatus.valueOf(rs.getString("to_status")),
                rs.getString("operator"),
                rs.getString("remark"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}

