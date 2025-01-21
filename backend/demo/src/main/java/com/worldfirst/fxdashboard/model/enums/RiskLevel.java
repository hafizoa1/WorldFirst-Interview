package com.worldfirst.fxdashboard.model.enums;

import lombok.Getter;

@Getter
public enum RiskLevel {
    LOW("Low risk position"),
    MEDIUM("Medium risk position"),
    HIGH("High risk position");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }
}
