package com.github.panteliszara.currencyconverter.ui;

import com.github.panteliszara.currencyconverter.domain.Currency;

/**
 * Input snapshot validated when conversion is requested.
 *
 * @param amount raw amount text
 * @param source selected source currency, or null if not selected
 * @param target selected target currency, or null if not selected
 */
public record ConversionInput(String amount, Currency source, Currency target) {
    /**
     * Reverses the currencies without changing the amount or fetching a rate.
     *
     * @return swapped input snapshot
     */
    public ConversionInput swapped() {
        return new ConversionInput(amount, target, source);
    }
}
