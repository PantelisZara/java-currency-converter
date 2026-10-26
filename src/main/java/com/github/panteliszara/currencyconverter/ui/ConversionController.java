package com.github.panteliszara.currencyconverter.ui;

import com.github.panteliszara.currencyconverter.api.ExchangeRateException;
import com.github.panteliszara.currencyconverter.api.ExchangeRateProvider;
import com.github.panteliszara.currencyconverter.domain.ConversionResult;
import com.github.panteliszara.currencyconverter.domain.ConversionService;
import com.github.panteliszara.currencyconverter.domain.ExchangeRate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

public final class ConversionController implements AutoCloseable {
    private final ExchangeRateProvider rates;
    private final ConversionService conversions;
    private final ExecutorService worker;
    private final Consumer<Runnable> dispatch;
    private final Consumer<ConversionState> display;
    private ConversionInput input;
    private ConversionState state = new ConversionState(ConversionState.Status.IDLE, "Ready when you are.", null);
    private Future<?> pending;
    private long generation;
    private boolean closed;

    public ConversionController(ExchangeRateProvider rates, ConversionService conversions,
                                ExecutorService worker, Consumer<Runnable> dispatch,
                                Consumer<ConversionState> display) {
        this.rates = Objects.requireNonNull(rates);
        this.conversions = Objects.requireNonNull(conversions);
        this.worker = Objects.requireNonNull(worker);
        this.dispatch = Objects.requireNonNull(dispatch);
        this.display = Objects.requireNonNull(display);
        display.accept(state);
    }

    public void inputChanged(ConversionInput updated) {
        if (closed) {
            return;
        }
        cancelPending();
        input = updated;
        publish(new ConversionState(ConversionState.Status.IDLE, "Inputs changed. Convert to see a new result.", null));
    }

    public void convert(ConversionInput requested) {
        if (closed || state.status() == ConversionState.Status.LOADING && Objects.equals(input, requested)) {
            return;
        }
        cancelPending();
        input = Objects.requireNonNull(requested);
        BigDecimal amount;
        try {
            if (requested.source() == null || requested.target() == null) {
                throw new IllegalArgumentException("Choose both currencies.");
            }
            amount = conversions.parseAmount(requested.amount());
        } catch (IllegalArgumentException e) {
            publish(error(e.getMessage()));
            return;
        }
        if (requested.source() == requested.target()) {
            publish(success(conversions.sameCurrency(amount, requested.source())));
            return;
        }
        publish(new ConversionState(ConversionState.Status.LOADING, "Fetching the latest available rate…", null));
        long requestGeneration = generation;
        try {
            pending = worker.submit(() -> fetch(requested, amount, requestGeneration));
        } catch (RejectedExecutionException e) {
            publish(error("The background worker is unavailable. Please restart the application."));
        }
    }

    private void fetch(ConversionInput requested, BigDecimal amount, long requestGeneration) {
        ConversionState completed;
        try {
            ExchangeRate rate = rates.fetch(requested.source(), requested.target());
            if (rate.base() != requested.source() || rate.quote() != requested.target()) {
                throw new ExchangeRateException("The rate service returned an unexpected currency pair.");
            }
            completed = success(conversions.convert(amount, rate));
        } catch (ExchangeRateException e) {
            completed = error(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            completed = error("The request was cancelled. Please try again.");
        } catch (RuntimeException e) {
            completed = error("Unable to complete the conversion. Please try again.");
        }
        ConversionState result = completed;
        dispatch.accept(() -> {
            if (!closed && requestGeneration == generation) {
                pending = null;
                publish(result);
            }
        });
    }

    private void cancelPending() {
        generation++;
        if (pending != null) {
            pending.cancel(true);
            pending = null;
        }
    }

    private void publish(ConversionState updated) {
        state = updated;
        display.accept(updated);
    }

    private static ConversionState error(String message) {
        return new ConversionState(ConversionState.Status.ERROR, message, null);
    }

    private static ConversionState success(ConversionResult result) {
        return new ConversionState(ConversionState.Status.SUCCESS, "Conversion complete.", result);
    }

    @Override
    public void close() {
        closed = true;
        cancelPending();
        worker.shutdownNow();
    }
}
