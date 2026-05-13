package com.observai.monitor.repository;

import com.observai.common.dto.ServiceMetricView;
import com.observai.common.enums.ServiceHealthStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceMetricRepository {
    private final JdbcTemplate jdbcTemplate;

    public ServiceMetricRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(ServiceMetricView metric) {
        jdbcTemplate.update(
                """
                INSERT INTO service_metrics
                  (service_name, cpu, memory, request_count, error_rate, response_time, status, timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                metric.serviceName(),
                metric.cpu(),
                metric.memory(),
                metric.requestCount(),
                metric.errorRate(),
                metric.responseTime(),
                metric.status().name(),
                metric.timestamp()
        );
    }

    public List<ServiceMetricView> findLatest() {
        return jdbcTemplate.query(
                """
                SELECT sm.service_name, sm.cpu, sm.memory, sm.request_count, sm.error_rate,
                       sm.response_time, sm.status, sm.timestamp
                FROM service_metrics sm
                JOIN (
                  SELECT service_name, MAX(timestamp) AS max_timestamp
                  FROM service_metrics
                  GROUP BY service_name
                ) latest
                  ON latest.service_name = sm.service_name
                 AND latest.max_timestamp = sm.timestamp
                ORDER BY sm.service_name
                """,
                this::mapRow
        );
    }

    public List<ServiceMetricView> findHistory(String serviceName, LocalDateTime start, LocalDateTime end) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT service_name, cpu, memory, request_count, error_rate, response_time, status, timestamp
                FROM service_metrics
                WHERE service_name = ?
                """
        );
        List<Object> args = new ArrayList<>();
        args.add(serviceName);
        if (start != null) {
            sql.append(" AND timestamp >= ?");
            args.add(start);
        }
        if (end != null) {
            sql.append(" AND timestamp <= ?");
            args.add(end);
        }
        sql.append(" ORDER BY timestamp");
        return jdbcTemplate.query(sql.toString(), this::mapRow, args.toArray());
    }

    public Optional<ServiceMetricView> latestOf(String serviceName) {
        try {
            ServiceMetricView metric = jdbcTemplate.queryForObject(
                    """
                    SELECT service_name, cpu, memory, request_count, error_rate, response_time, status, timestamp
                    FROM service_metrics
                    WHERE service_name = ?
                    ORDER BY timestamp DESC
                    LIMIT 1
                    """,
                    this::mapRow,
                    serviceName
            );
            return Optional.ofNullable(metric);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private ServiceMetricView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ServiceMetricView(
                rs.getString("service_name"),
                rs.getDouble("cpu"),
                rs.getDouble("memory"),
                rs.getLong("request_count"),
                rs.getDouble("error_rate"),
                rs.getDouble("response_time"),
                ServiceHealthStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}
