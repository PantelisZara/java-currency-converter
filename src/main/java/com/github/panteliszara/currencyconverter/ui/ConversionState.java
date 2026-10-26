package com.github.panteliszara.currencyconverter.ui;

import com.github.panteliszara.currencyconverter.domain.ConversionResult;

public record ConversionState(Status status, String message, ConversionResult result) {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }
}
