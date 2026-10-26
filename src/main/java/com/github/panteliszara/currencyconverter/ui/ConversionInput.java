package com.github.panteliszara.currencyconverter.ui;

import com.github.panteliszara.currencyconverter.domain.Currency;

public record ConversionInput(String amount, Currency source, Currency target) {
    public ConversionInput swapped() {
        return new ConversionInput(amount, target, source);
    }
}
