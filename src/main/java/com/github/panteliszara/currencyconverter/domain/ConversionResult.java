package com.github.panteliszara.currencyconverter.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public record ConversionResult(BigDecimal amount, Currency source, Currency target,
                               BigDecimal convertedAmount, BigDecimal rate,
                               Optional<LocalDate> rateDate) {
}
