package com.observai.common.enums;

public enum Severity {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    Severity(int level) {
        this.level = level;
    }

    public boolean atLeast(Severity minimum) {
        return this.level >= minimum.level;
    }
}

