package com.observai.notification.repository;

import com.observai.common.enums.Severity;
import com.observai.notification.model.NotificationConfig;
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
public class NotificationConfigRepository {
    private final JdbcTemplate jdbcTemplate;

    public NotificationConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NotificationConfig> findAll() {
        return jdbcTemplate.query(
                """
                SELECT id, email, min_severity, enabled, created_at, updated_at
                FROM notification_config
                ORDER BY id
                """,
                this::mapRow
        );
    }

    public List<NotificationConfig> findEnabled() {
        return jdbcTemplate.query(
                """
                SELECT id, email, min_severity, enabled, created_at, updated_at
                FROM notification_config
                WHERE enabled = 1
                ORDER BY id
                """,
                this::mapRow
        );
    }

    public Optional<NotificationConfig> findById(Long id) {
        try {
            NotificationConfig config = jdbcTemplate.queryForObject(
                    """
                    SELECT id, email, min_severity, enabled, created_at, updated_at
                    FROM notification_config
                    WHERE id = ?
                    """,
                    this::mapRow,
                    id
            );
            return Optional.ofNullable(config);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public NotificationConfig save(NotificationConfig config) {
        if (config.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        """
                        INSERT INTO notification_config (email, min_severity, enabled)
                        VALUES (?, ?, ?)
                        """,
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, config.getEmail());
                ps.setString(2, config.getMinSeverity().name());
                ps.setInt(3, config.isEnabled() ? 1 : 0);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                config.setId(key.longValue());
            }
            return findById(config.getId()).orElse(config);
        }

        jdbcTemplate.update(
                """
                UPDATE notification_config
                SET email = ?, min_severity = ?, enabled = ?
                WHERE id = ?
                """,
                config.getEmail(),
                config.getMinSeverity().name(),
                config.isEnabled() ? 1 : 0,
                config.getId()
        );
        return findById(config.getId()).orElse(config);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM notification_config WHERE id = ?", id);
    }

    private NotificationConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
        NotificationConfig config = new NotificationConfig();
        config.setId(rs.getLong("id"));
        config.setEmail(rs.getString("email"));
        config.setMinSeverity(Severity.valueOf(rs.getString("min_severity")));
        config.setEnabled(rs.getInt("enabled") == 1);
        config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return config;
    }
}

