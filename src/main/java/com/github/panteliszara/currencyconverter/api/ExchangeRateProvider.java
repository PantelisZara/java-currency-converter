package com.github.panteliszara.currencyconverter.api;

import com.github.panteliszara.currencyconverter.domain.Currency;
import com.github.panteliszara.currencyconverter.domain.ExchangeRate;

@FunctionalInterface
public interface ExchangeRateProvider {
    ExchangeRate fetch(Currency base, Currency quote) throws ExchangeRateException, InterruptedException;
}
