package com.worldfirst.fxdashboard.service;

import com.worldfirst.fxdashboard.model.*;
import com.worldfirst.fxdashboard.model.enums.AlertLevel;
import com.worldfirst.fxdashboard.model.enums.AlertStatus;
import com.worldfirst.fxdashboard.model.enums.RiskLevel;
import com.worldfirst.fxdashboard.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FXRiskService {
    private final CurrencyPositionRepository positionRepository;
    private final RateRepository rateRepository;
    //private final RateCache rateCache;

    @Transactional(readOnly = true)
    public List<CurrencyPosition> getAllPositions() {
        System.out.println(positionRepository.findAll());
        return positionRepository.findAll().stream()
                .map(this::enrichWithCurrentRates)
                .collect(Collectors.toList());
    }

    @Transactional
    public CurrencyPosition updatePosition(CurrencyPosition position) {
        //validatePosition(position);
        calculateRiskLevel(position);
        return positionRepository.save(position);
    }

    @Transactional(readOnly = true)
    public List<RiskAlert> generateRiskAlerts() {
        List<RiskAlert> alerts = new ArrayList<>();

        // Check for high-risk positions
        positionRepository.findByRiskLevel(RiskLevel.HIGH)
                .forEach(position -> alerts.add(createHighRiskAlert(position)));

        // Check for low balance positions
        positionRepository.findLowBalancePositions(new BigDecimal("50000"))
                .forEach(position -> alerts.add(createLowBalanceAlert(position)));

        return alerts;
    }

    private CurrencyPosition enrichWithCurrentRates(CurrencyPosition position) {
        // Skip rate lookup for USD since it's the base currency
        if (position.getCurrency().equals("USD")) {
            position.setCurrentRate(BigDecimal.ONE);  // USD/USD rate is always 1.0
            position.setRateTimestamp(LocalDateTime.now());
            return position;
        }

        String currencyPair = formatCurrencyPair(position.getCurrency());
        ExchangeRate latestRate = rateRepository
                .findTopByCurrencyPairOrderByTimestampDesc(currencyPair)
                .orElseThrow(() -> new RuntimeException("Rate not found for " + currencyPair));

        position.setCurrentRate(latestRate.getRate());
        position.setRateTimestamp(latestRate.getTimestamp());

        return position;
    }

    private String formatCurrencyPair(String currency) {
        // USD is typically the quote currency except for EUR and GBP
        if (currency.equals("EUR") || currency.equals("GBP")) {
            return currency + "USD";
        }
        // For other non-USD currencies, USD is the base currency
        return "USD" + currency;
    }

    private void calculateRiskLevel(CurrencyPosition position) {
        BigDecimal netPosition = position.getBalance()
                .add(position.getPendingIncome())
                .subtract(position.getPendingPayments());

        BigDecimal threshold = new BigDecimal("1000000"); // $1M threshold

        if (netPosition.abs().compareTo(threshold) > 0) {
            position.setRiskLevel(RiskLevel.HIGH);
        } else if (netPosition.abs().compareTo(threshold.divide(BigDecimal.valueOf(2))) > 0) {
            position.setRiskLevel(RiskLevel.MEDIUM);
        } else {
            position.setRiskLevel(RiskLevel.LOW);
        }
    }

    private RiskAlert createHighRiskAlert(CurrencyPosition position) {
        return RiskAlert.builder()
                .level(AlertLevel.HIGH)
                .message(String.format("High risk position in %s: %s",
                        position.getCurrency(),
                        position.getBalance()))
                .recommendation(generateRecommendation(position))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private RiskAlert createLowBalanceAlert(CurrencyPosition position) {
        BigDecimal netBalance = position.getBalance()
                .add(position.getPendingIncome())
                .subtract(position.getPendingPayments());

        return RiskAlert.builder()
                .level(AlertLevel.MEDIUM)  // Medium alert as it needs attention but might not be critical
                .message(String.format("Low balance alert for %s: Current net position %s",
                        position.getCurrency(),
                        netBalance))
                .recommendation(generateRecommendation(position))
                .currency(position.getCurrency())
                .timestamp(LocalDateTime.now())
                .triggeredBy("LOW_BALANCE")
                .thresholdValue(50000.0)  // Matches the threshold from findLowBalancePositions
                .actualValue(netBalance.doubleValue())
                .status(AlertStatus.ACTIVE)
                .build();
    }

    private String generateRecommendation(CurrencyPosition position) {
        BigDecimal netPosition = position.getBalance()
                .add(position.getPendingIncome())
                .subtract(position.getPendingPayments());

        if (netPosition.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("Consider reducing %s position by converting to other currencies",
                    position.getCurrency());
        } else {
            return String.format("Consider acquiring more %s to cover upcoming payments",
                    position.getCurrency());
        }
    }

    public Optional<CurrencyPosition> getPosition(String currency) {
        return positionRepository.findByCurrency(currency)
                .map(this::enrichWithCurrentRates);
    }
}
