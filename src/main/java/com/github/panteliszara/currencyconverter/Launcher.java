package com.github.panteliszara.currencyconverter;

import javafx.application.Application;

/** Starts JavaFX from the packaged application. */
public final class Launcher {
    private Launcher() {
    }

    /**
     * Starts the application with the packaged JavaFX runtime.
     *
     * @param args command-line arguments forwarded to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(CurrencyConverterApp.class, args);
    }
}
