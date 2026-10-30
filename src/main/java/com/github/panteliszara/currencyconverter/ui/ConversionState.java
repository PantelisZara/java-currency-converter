package com.github.panteliszara.currencyconverter.ui;

import com.github.panteliszara.currencyconverter.domain.ConversionResult;

/**
 * State passed from the controller to the view.
 *
 * @param status current conversion stage
 * @param message loading or error feedback, empty for idle and success
 * @param result conversion data for success, otherwise null
 */
public record ConversionState(Status status, String message, ConversionResult result) {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }
}
