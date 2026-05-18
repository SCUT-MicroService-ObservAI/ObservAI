package com.observai.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "observai.alert")
public class AlertProperties {
    private int notificationSuppressionMinutes = 30;

    public int getNotificationSuppressionMinutes() {
        return notificationSuppressionMinutes;
    }

    public void setNotificationSuppressionMinutes(int notificationSuppressionMinutes) {
        this.notificationSuppressionMinutes = notificationSuppressionMinutes;
    }
}
