package com.github.panteliszara.currencyconverter.api;

public final class ExchangeRateException extends Exception {
    private static final long serialVersionUID = 1L;

    public ExchangeRateException(String message) {
        super(message);
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }
}
