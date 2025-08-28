package com.jobhuntly.backend.entity.enums;

public enum ReportType {
    JOB(0),
    USER(1),
    COMPANY(2);

    private final int value;

    ReportType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ReportType fromValue(int value) {
        for (ReportType type : values()) {
            if (type.value == value) return type;
        }
        throw new IllegalArgumentException("Invalid ReportType value: " + value);
    }
}
