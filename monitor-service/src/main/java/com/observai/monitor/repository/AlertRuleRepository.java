package com.observai.monitor.repository;

import com.observai.common.enums.Severity;
import com.observai.monitor.model.AlertRule;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRuleRepository {
    private final JdbcTemplate jdbcTemplate;

    public AlertRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AlertRule> findAll() {
        return jdbcTemplate.query(
                """
                SELECT id, service_name, metric_name, operator, threshold, duration_seconds,
                       severity, enabled, created_at, updated_at
                FROM alert_rule
                ORDER BY id
                """,
                this::mapRow
        );
    }

    public List<AlertRule> findEnabledByService(String serviceName) {
        return jdbcTemplate.query(
                """
                SELECT id, service_name, metric_name, operator, threshold, duration_seconds,
                       severity, enabled, created_at, updated_at
                FROM alert_rule
                WHERE enabled = 1 AND service_name = ?
                ORDER BY id
                """,
                this::mapRow,
                serviceName
        );
    }

    public Optional<AlertRule> findById(Long id) {
        try {
            AlertRule rule = jdbcTemplate.queryForObject(
                    """
                    SELECT id, service_name, metric_name, operator, threshold, duration_seconds,
                           severity, enabled, created_at, updated_at
                    FROM alert_rule
                    WHERE id = ?
                    """,
                    this::mapRow,
                    id
            );
            return Optional.ofNullable(rule);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public AlertRule save(AlertRule rule) {
        if (rule.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        """
                        INSERT INTO alert_rule
                          (service_name, metric_name, operator, threshold, duration_seconds, severity, enabled)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                        Statement.RETURN_GENERATED_KEYS
                );
                bindRule(ps, rule);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                rule.setId(key.longValue());
            }
            return findById(rule.getId()).orElse(rule);
        }

        jdbcTemplate.update(
                """
                UPDATE alert_rule
                SET service_name = ?, metric_name = ?, operator = ?, threshold = ?,
                    duration_seconds = ?, severity = ?, enabled = ?
                WHERE id = ?
                """,
                rule.getServiceName(),
                rule.getMetricName(),
                rule.getOperator(),
                rule.getThreshold(),
                rule.getDurationSeconds(),
                rule.getSeverity().name(),
                rule.isEnabled() ? 1 : 0,
                rule.getId()
        );
        return findById(rule.getId()).orElse(rule);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM alert_rule WHERE id = ?", id);
    }

    private void bindRule(PreparedStatement ps, AlertRule rule) throws SQLException {
        ps.setString(1, rule.getServiceName());
        ps.setString(2, rule.getMetricName());
        ps.setString(3, rule.getOperator());
        ps.setDouble(4, rule.getThreshold());
        ps.setInt(5, rule.getDurationSeconds());
        ps.setString(6, rule.getSeverity().name());
        ps.setInt(7, rule.isEnabled() ? 1 : 0);
    }

    private AlertRule mapRow(ResultSet rs, int rowNum) throws SQLException {
        AlertRule rule = new AlertRule();
        rule.setId(rs.getLong("id"));
        rule.setServiceName(rs.getString("service_name"));
        rule.setMetricName(rs.getString("metric_name"));
        rule.setOperator(rs.getString("operator"));
        rule.setThreshold(rs.getDouble("threshold"));
        rule.setDurationSeconds(rs.getInt("duration_seconds"));
        rule.setSeverity(Severity.valueOf(rs.getString("severity")));
        rule.setEnabled(rs.getInt("enabled") == 1);
        rule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        rule.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return rule;
    }
}

