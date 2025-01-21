package com.worldfirst.fxdashboard.model;

import com.worldfirst.fxdashboard.model.enums.AlertLevel;
import com.worldfirst.fxdashboard.model.enums.AlertStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlert {
    private Long id;
    private AlertLevel level;
    private String message;
    private String recommendation;
    private String currency;
    private String triggeredBy;  // What caused this alert
    private Double thresholdValue;  // The threshold that was breached
    private Double actualValue;  // The actual value that triggered the alert
    private LocalDateTime timestamp;
    private LocalDateTime expiresAt;
    private LocalDateTime resolvedAt;
    private AlertStatus status;

    // Factory method for creating new alerts
    public static RiskAlert createNew() {
        RiskAlert alert = new RiskAlert();
        alert.setTimestamp(LocalDateTime.now());
        alert.setStatus(AlertStatus.ACTIVE);
        return alert;
    }

    // Business methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isResolved() {
        return resolvedAt != null || status == AlertStatus.RESOLVED;
    }

    public void resolve() {
        this.resolvedAt = LocalDateTime.now();
        this.status = AlertStatus.RESOLVED;
    }

    public void acknowledge() {
        if (this.status == AlertStatus.ACTIVE) {
            this.status = AlertStatus.ACKNOWLEDGED;
        }
    }

    // Validation
    public void validate() {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
        if (level == null) {
            throw new IllegalArgumentException("Alert level must be specified");
        }
    }
}