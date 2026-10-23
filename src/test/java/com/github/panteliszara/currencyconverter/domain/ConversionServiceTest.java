package com.github.panteliszara.currencyconverter.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ConversionServiceTest {
    private final ConversionService service = new ConversionService();
    private final LocalDate date = LocalDate.of(2026, 10, 20);

    @ParameterizedTest
    @CsvSource({
            "10, 1.23456, USD, 12.35",
            "0.1, 0.2, USD, 0.02",
            "2.345, 1, CHF, 2.35",
            "2.3449, 1, GBP, 2.34",
            "12.5, 1, JPY, 13",
            "12.4999, 1, JPY, 12",
            "0.0001, 0.5, USD, 0.00",
            "0.0001, 10000, USD, 1.00",
            "999999999999.9999, 1.25, USD, 1250000000000.00"
    })
    void convertsWithDecimalArithmeticAndFinalRounding(String amount, String rate,
                                                       Currency target, String expected) {
        ConversionResult result = service.convert(new BigDecimal(amount),
                new ExchangeRate(Currency.EUR, target, new BigDecimal(rate), date));
        assertEquals(new BigDecimal(expected), result.convertedAmount());
        assertEquals(new BigDecimal(rate), result.rate());
        assertEquals(date, result.rateDate().orElseThrow());
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void usesTheTargetCurrencyScale(Currency target) {
        ConversionResult result = service.convert(new BigDecimal("1"),
                new ExchangeRate(Currency.USD, target, new BigDecimal("1.235"), date));
        assertEquals(new BigDecimal(target == Currency.JPY ? "1" : "1.24"), result.convertedAmount());
    }

    @Test
    void sameCurrencyUsesIdentityRateWithoutAnInventedDate() {
        ConversionResult result = service.sameCurrency(new BigDecimal("10.125"), Currency.EUR);
        assertEquals(new BigDecimal("10.13"), result.convertedAmount());
        assertEquals(BigDecimal.ONE, result.rate());
        assertEquals(Currency.EUR, result.source());
        assertEquals(Currency.EUR, result.target());
        assertTrue(result.rateDate().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"0.0001, 0.0001", "999999999999.9999, 999999999999.9999", "' 12.3400 ', 12.3400"})
    void parsesInclusiveBoundariesAndOuterWhitespace(String text, String expected) {
        assertEquals(new BigDecimal(expected), service.parseAmount(text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "0", "0.0000", "-1", "+1", "1,25", ".5", "1.", "1e3",
            "NaN", "1 000", "1000000000000", "0000000000001", "1.00001", "1..2"})
    void rejectsInvalidInput(String text) {
        assertThrows(IllegalArgumentException.class, () -> service.parseAmount(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.1", "1000000000000", "0.00001"})
    void serviceAlsoEnforcesConstraintsForDirectCallers(String amount) {
        assertThrows(IllegalArgumentException.class,
                () -> service.sameCurrency(new BigDecimal(amount), Currency.USD));
    }

    @Test
    void rejectsNullAmountAndNullRate() {
        assertThrows(IllegalArgumentException.class, () -> service.sameCurrency(null, Currency.USD));
        assertThrows(IllegalArgumentException.class,
                () -> new ExchangeRate(Currency.EUR, Currency.USD, null, date));
        assertThrows(NullPointerException.class, () -> service.convert(BigDecimal.ONE, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.001"})
    void rejectsNonpositiveRates(String rate) {
        assertThrows(IllegalArgumentException.class,
                () -> new ExchangeRate(Currency.EUR, Currency.USD, new BigDecimal(rate), date));
    }
}
