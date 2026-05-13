package com.observai.user.repository;

import com.observai.user.model.UserAccount;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findByUsername(String username) {
        try {
            UserAccount user = jdbcTemplate.queryForObject(
                    """
                    SELECT user_id, username, password, created_at, updated_at
                    FROM users
                    WHERE username = ?
                    """,
                    this::mapRow,
                    username
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private UserAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserAccount(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}

