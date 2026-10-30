package com.github.panteliszara.currencyconverter.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A positive dated rate in quote-currency units per base-currency unit.
 *
 * @param base currency being exchanged
 * @param quote currency whose units express the rate
 * @param value positive number of quote-currency units per base-currency unit
 * @param date effective date reported by the rate provider
 */
public record ExchangeRate(Currency base, Currency quote, BigDecimal value, LocalDate date) {
    /**
     * Validates that the pair and date are present and the rate is positive.
     *
     * @throws NullPointerException if either currency or the date is null
     * @throws IllegalArgumentException if the value is null, zero, or negative
     */
    public ExchangeRate {
        Objects.requireNonNull(base, "Base currency is required");
        Objects.requireNonNull(quote, "Quote currency is required");
        Objects.requireNonNull(date, "Rate date is required");
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive.");
        }
    }
}
