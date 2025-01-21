package com.worldfirst.fxdashboard.controller;

import com.worldfirst.fxdashboard.model.CurrencyPosition;
import com.worldfirst.fxdashboard.model.RiskAlert;
import com.worldfirst.fxdashboard.service.FXRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fx")
@RequiredArgsConstructor
public class FXRiskController {
    private final FXRiskService fxRiskService;

    @GetMapping("/positions")
    public ResponseEntity<List<CurrencyPosition>> getAllPositions() {
        return ResponseEntity.ok(fxRiskService.getAllPositions());
    }

    @GetMapping("/position/{currency}")
    public ResponseEntity<CurrencyPosition> getPosition(@PathVariable String currency) {
        return fxRiskService.getPosition(currency)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/position")
    public ResponseEntity<CurrencyPosition> updatePosition(
            @RequestBody CurrencyPosition position) {
        return ResponseEntity.ok(fxRiskService.updatePosition(position));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<RiskAlert>> getRiskAlerts() {
        return ResponseEntity.ok(fxRiskService.generateRiskAlerts());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("positions", fxRiskService.getAllPositions());
        dashboard.put("alerts", fxRiskService.generateRiskAlerts());
        dashboard.put("lastUpdated", LocalDateTime.now());
        return ResponseEntity.ok(dashboard);
    }
}