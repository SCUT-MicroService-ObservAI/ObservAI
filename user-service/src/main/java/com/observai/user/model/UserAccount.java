package com.observai.user.model;

import java.time.LocalDateTime;

public record UserAccount(
        Long userId,
        String username,
        String password,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

