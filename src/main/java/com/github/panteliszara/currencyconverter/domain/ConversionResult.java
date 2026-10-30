package com.github.panteliszara.currencyconverter.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * A conversion with its rate metadata.
 *
 * @param amount original amount in the source currency
 * @param source currency being exchanged
 * @param target currency of the converted amount
 * @param convertedAmount result rounded to the target currency's fraction digits
 * @param rate target-currency units per unit of source currency
 * @param rateDate effective rate date, empty for a same-currency conversion
 */
public record ConversionResult(BigDecimal amount, Currency source, Currency target,
                               BigDecimal convertedAmount, BigDecimal rate,
                               Optional<LocalDate> rateDate) {
}
