package com.github.panteliszara.currencyconverter.domain;

public enum Currency {
    EUR("Euro", 2),
    USD("US Dollar", 2),
    GBP("British Pound", 2),
    CHF("Swiss Franc", 2),
    JPY("Japanese Yen", 0);

    private final String displayName;
    private final int fractionDigits;

    Currency(String displayName, int fractionDigits) {
        this.displayName = displayName;
        this.fractionDigits = fractionDigits;
    }

    public int fractionDigits() {
        return fractionDigits;
    }

    @Override
    public String toString() {
        return name() + " — " + displayName;
    }
}
