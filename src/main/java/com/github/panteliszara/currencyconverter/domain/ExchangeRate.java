package com.github.panteliszara.currencyconverter.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record ExchangeRate(Currency base, Currency quote, BigDecimal value, LocalDate date) {
    public ExchangeRate {
        Objects.requireNonNull(base, "Base currency is required");
        Objects.requireNonNull(quote, "Quote currency is required");
        Objects.requireNonNull(date, "Rate date is required");
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive.");
        }
    }
}
