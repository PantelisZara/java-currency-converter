package com.github.panteliszara.currencyconverter.api;

/** A transport, service, or response validation failure. */
public final class ExchangeRateException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a failure with a user-facing message.
     *
     * @param message explanation of the lookup failure
     */
    public ExchangeRateException(String message) {
        super(message);
    }

    /**
     * Creates a failure with its cause.
     *
     * @param message explanation of the lookup failure
     * @param cause underlying transport or response validation error
     */
    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }
}
