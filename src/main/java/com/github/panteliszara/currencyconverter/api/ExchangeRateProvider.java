package com.github.panteliszara.currencyconverter.api;

import com.github.panteliszara.currencyconverter.domain.Currency;
import com.github.panteliszara.currencyconverter.domain.ExchangeRate;

/** Supplies dated exchange rates. */
@FunctionalInterface
public interface ExchangeRateProvider {
    /**
     * Fetches quote-currency units per base-currency unit; may block, so call off the UI thread.
     *
     * @param base currency being exchanged
     * @param quote currency whose units express the rate
     * @return rate and its effective date for the requested pair
     * @throws ExchangeRateException if a usable rate cannot be obtained
     * @throws InterruptedException if the lookup is interrupted; callers must handle cancellation
     */
    ExchangeRate fetch(Currency base, Currency quote) throws ExchangeRateException, InterruptedException;
}
