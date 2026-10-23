package com.github.panteliszara.currencyconverter.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ConversionService {
    private static final Pattern AMOUNT = Pattern.compile("[0-9]{1,12}(?:\\.[0-9]{1,4})?");

    public BigDecimal parseAmount(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Enter an amount.");
        }
        String normalized = text.strip();
        if (!AMOUNT.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Use up to 12 integer digits and 4 decimal places, with a decimal point.");
        }
        BigDecimal amount = new BigDecimal(normalized);
        validateAmount(amount);
        return amount;
    }

    public ConversionResult convert(BigDecimal amount, ExchangeRate rate) {
        validateAmount(amount);
        Objects.requireNonNull(rate, "Exchange rate is required");
        return new ConversionResult(amount, rate.base(), rate.quote(),
                amount.multiply(rate.value()).setScale(rate.quote().fractionDigits(), RoundingMode.HALF_UP),
                rate.value(), Optional.of(rate.date()));
    }

    public ConversionResult sameCurrency(BigDecimal amount, Currency currency) {
        validateAmount(amount);
        Objects.requireNonNull(currency, "Currency is required");
        return new ConversionResult(amount, currency, currency,
                amount.setScale(currency.fractionDigits(), RoundingMode.HALF_UP),
                BigDecimal.ONE, Optional.empty());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (amount.scale() > 4 || (long) amount.precision() - amount.scale() > 12) {
            throw new IllegalArgumentException("Amount exceeds 12 integer digits or 4 decimal places.");
        }
    }
}
