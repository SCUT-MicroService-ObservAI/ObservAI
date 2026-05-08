package com.observai.common.enums;

public enum AlertStatus {
    UNHANDLED,
    PROCESSING,
    RESOLVED,
    IGNORED,
    FALSE_ALARM,
    RECOVERED;

    public boolean isTerminal() {
        return this == RESOLVED || this == IGNORED || this == FALSE_ALARM || this == RECOVERED;
    }
}

