package com.observai.notification.model;

import com.observai.common.enums.Severity;
import java.time.LocalDateTime;

public class NotificationConfig {
    private Long id;
    private String email;
    private Severity minSeverity;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Severity getMinSeverity() { return minSeverity; }
    public void setMinSeverity(Severity minSeverity) { this.minSeverity = minSeverity; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

