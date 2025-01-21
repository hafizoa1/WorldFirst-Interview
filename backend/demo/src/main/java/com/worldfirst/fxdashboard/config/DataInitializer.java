package com.worldfirst.fxdashboard.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Initializing mock data...");
        dropTablesIfExist();
        createTables();
        initializeCurrencyPositions();
        initializeExchangeRates();
        log.info("Data initialization completed");
    }

    private void dropTablesIfExist() {
        log.info("Dropping existing tables if they exist...");
        jdbcTemplate.execute("DROP TABLE IF EXISTS currency_positions CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS exchange_rates CASCADE");
    }

    private void createTables() {
        log.info("Creating tables...");

        jdbcTemplate.execute("""
            CREATE TABLE currency_positions (
                id BIGSERIAL PRIMARY KEY,
                currency VARCHAR(3) NOT NULL,
                balance DECIMAL(19,4) NOT NULL,
                pending_income DECIMAL(19,4),
                pending_payments DECIMAL(19,4),
                risk_level VARCHAR(10) NOT NULL,
                current_rate DECIMAL(10,6),
                rate_timestamp TIMESTAMP,
                last_updated TIMESTAMP NOT NULL
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE exchange_rates (
                id BIGSERIAL PRIMARY KEY,
                currency_pair VARCHAR(7) NOT NULL,
                rate DECIMAL(10,6) NOT NULL,
                bid DECIMAL(10,6),
                ask DECIMAL(10,6),
                timestamp TIMESTAMP NOT NULL,
                source VARCHAR(50),
                volatility_index DECIMAL(10,6)
            )
        """);

        // Add indexes for better performance
        jdbcTemplate.execute("CREATE INDEX idx_currency_positions_currency ON currency_positions(currency)");
        jdbcTemplate.execute("CREATE INDEX idx_exchange_rates_currency_pair ON exchange_rates(currency_pair)");
        jdbcTemplate.execute("CREATE INDEX idx_exchange_rates_timestamp ON exchange_rates(timestamp)");

        log.info("Tables created successfully");
    }

    private void initializeCurrencyPositions() {
        log.info("Initializing currency positions...");

        String sql = """
            INSERT INTO currency_positions 
            (currency, balance, pending_income, pending_payments, risk_level, current_rate, rate_timestamp, last_updated) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        LocalDateTime now = LocalDateTime.now();
        List<Object[]> positionData = Arrays.asList(
                new Object[] {
                        "USD",
                        new BigDecimal("1000000.00"),
                        new BigDecimal("50000.00"),
                        new BigDecimal("30000.00"),
                        "HIGH",
                        new BigDecimal("1.0"),
                        now,  // rate_timestamp
                        now   // last_updated
                },
                new Object[] {
                        "EUR",
                        new BigDecimal("800000.00"),
                        new BigDecimal("25000.00"),
                        new BigDecimal("75000.00"),
                        "MEDIUM",
                        new BigDecimal("0.85"),
                        now,
                        now
                },
                new Object[] {
                        "GBP",
                        new BigDecimal("600000.00"),
                        new BigDecimal("15000.00"),
                        new BigDecimal("45000.00"),
                        "LOW",
                        new BigDecimal("0.79"),
                        now,
                        now
                },
                new Object[] {
                        "JPY",
                        new BigDecimal("50000000.00"),
                        new BigDecimal("1000000.00"),
                        new BigDecimal("2000000.00"),
                        "MEDIUM",
                        new BigDecimal("150.0"),
                        now,
                        now
                },
                new Object[] {
                        "CNY",
                        new BigDecimal("2000000.00"),
                        new BigDecimal("100000.00"),
                        new BigDecimal("300000.00"),
                        "HIGH",
                        new BigDecimal("7.2"),
                        now,
                        now
                }
        );

        try {
            jdbcTemplate.batchUpdate(sql, positionData);
            log.info("Initialized {} currency positions", positionData.size());
        } catch (Exception e) {
            log.error("Error initializing currency positions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize currency positions", e);
        }
    }

    private void initializeExchangeRates() {
        log.info("Initializing exchange rates...");

        String sql = """
            INSERT INTO exchange_rates 
            (currency_pair, rate, bid, ask, timestamp, source, volatility_index) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        LocalDateTime now = LocalDateTime.now();
        List<Object[]> rateData = Arrays.asList(
                // Current rates
                new Object[] {
                        "EURUSD",
                        new BigDecimal("1.0850"),
                        new BigDecimal("1.0848"),
                        new BigDecimal("1.0852"),
                        now,
                        "MOCK_DATA",
                        new BigDecimal("0.12")
                },
                new Object[] {
                        "GBPUSD",
                        new BigDecimal("1.2650"),
                        new BigDecimal("1.2648"),
                        new BigDecimal("1.2652"),
                        now,
                        "MOCK_DATA",
                        new BigDecimal("0.15")
                },
                new Object[] {
                        "USDJPY",
                        new BigDecimal("150.50"),
                        new BigDecimal("150.48"),
                        new BigDecimal("150.52"),
                        now,
                        "MOCK_DATA",
                        new BigDecimal("0.18")
                },
                new Object[] {
                        "USDCNY",
                        new BigDecimal("7.2010"),
                        new BigDecimal("7.2008"),
                        new BigDecimal("7.2012"),
                        now,
                        "MOCK_DATA",
                        new BigDecimal("0.08")
                },
                // Historical rates
                new Object[] {
                        "EURUSD",
                        new BigDecimal("1.0855"),
                        new BigDecimal("1.0853"),
                        new BigDecimal("1.0857"),
                        now.minusMinutes(5),
                        "MOCK_DATA",
                        new BigDecimal("0.11")
                },
                new Object[] {
                        "GBPUSD",
                        new BigDecimal("1.2645"),
                        new BigDecimal("1.2643"),
                        new BigDecimal("1.2647"),
                        now.minusMinutes(5),
                        "MOCK_DATA",
                        new BigDecimal("0.14")
                }
        );

        try {
            jdbcTemplate.batchUpdate(sql, rateData);
            log.info("Initialized {} exchange rates", rateData.size());
        } catch (Exception e) {
            log.error("Error initializing exchange rates: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize exchange rates", e);
        }
    }
}