package com.worldfirst.fxdashboard.repository;


import com.worldfirst.fxdashboard.model.ExchangeRate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RateRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_LATEST_RATE = """
            SELECT * FROM exchange_rates 
            WHERE currency_pair = ? 
            ORDER BY timestamp DESC LIMIT 1
            """;

    private static final String INSERT_RATE = """
            INSERT INTO exchange_rates (
                currency_pair, rate, timestamp, source
            ) VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_RATES_BY_TIMERANGE = """
            SELECT * FROM exchange_rates 
            WHERE currency_pair = ? 
            AND timestamp BETWEEN ? AND ?
            ORDER BY timestamp DESC
            """;

    private final RowMapper<ExchangeRate> rateMapper = (rs, rowNum) -> {
        ExchangeRate rate = new ExchangeRate();
        rate.setId(rs.getLong("id"));
        rate.setCurrencyPair(rs.getString("currency_pair"));
        rate.setRate(rs.getBigDecimal("rate"));
        rate.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        rate.setSource(rs.getString("source"));
        return rate;
    };

    public Optional<ExchangeRate> findLatestRate(String currencyPair) {
        List<ExchangeRate> rates = jdbcTemplate.query(
                SELECT_LATEST_RATE,
                rateMapper,
                currencyPair
        );
        return rates.isEmpty() ? Optional.empty() : Optional.of(rates.get(0));
    }

    public ExchangeRate save(ExchangeRate rate) {
        jdbcTemplate.update(
                INSERT_RATE,
                rate.getCurrencyPair(),
                rate.getRate(),
                rate.getTimestamp(),
                rate.getSource()
        );
        return rate;
    }

    public List<ExchangeRate> findRatesByTimeRange(
            String currencyPair,
            LocalDateTime start,
            LocalDateTime end) {
        return jdbcTemplate.query(
                SELECT_RATES_BY_TIMERANGE,
                rateMapper,
                currencyPair,
                start,
                end
        );
    }

    public Optional<ExchangeRate> findTopByCurrencyPairOrderByTimestampDesc(String currencyPair) {
        List<ExchangeRate> rates = jdbcTemplate.query(
                SELECT_LATEST_RATE,
                rateMapper,
                currencyPair
        );
        return rates.isEmpty() ? Optional.empty() : Optional.of(rates.get(0));
    }
}
