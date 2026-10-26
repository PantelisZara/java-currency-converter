package com.github.panteliszara.currencyconverter.ui;

import com.github.panteliszara.currencyconverter.api.ExchangeRateException;
import com.github.panteliszara.currencyconverter.api.ExchangeRateProvider;
import com.github.panteliszara.currencyconverter.domain.ConversionService;
import com.github.panteliszara.currencyconverter.domain.Currency;
import com.github.panteliszara.currencyconverter.domain.ExchangeRate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.panteliszara.currencyconverter.ui.ConversionState.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class ConversionControllerTest {
    private final ManualExecutor worker = new ManualExecutor();
    private final List<Runnable> uiQueue = new ArrayList<>();
    private final List<ConversionState> states = new ArrayList<>();
    private final AtomicInteger requests = new AtomicInteger();
    private final ConversionInput input = new ConversionInput("10", Currency.EUR, Currency.USD);
    private ConversionController controller;

    private void create(ExchangeRateProvider provider) {
        controller = new ConversionController(provider, new ConversionService(), worker, uiQueue::add, states::add);
    }

    private void create() {
        create((base, quote) -> {
            requests.incrementAndGet();
            return new ExchangeRate(base, quote, new BigDecimal("1.25"), LocalDate.of(2026, 10, 20));
        });
    }

    private ConversionState state() {
        return states.getLast();
    }

    private void finish() {
        worker.runNext();
        uiQueue.removeFirst().run();
    }

    @AfterEach
    void close() {
        if (controller != null) {
            controller.close();
        }
    }

    @Test
    void runsRequestsOnWorkerAndDefersResultsToUiDispatcher() {
        create();
        controller.convert(input);
        assertEquals(LOADING, state().status());
        assertEquals(0, requests.get());
        worker.runNext();
        assertEquals(LOADING, state().status());
        assertEquals(1, requests.get());
        uiQueue.removeFirst().run();
        assertEquals(SUCCESS, state().status());
        assertEquals(new BigDecimal("12.50"), state().result().convertedAmount());
    }

    @Test
    void repeatedClicksDoNotCreateDuplicateRequests() {
        create();
        controller.convert(input);
        controller.convert(input);
        assertEquals(1, worker.tasks.size());
        finish();
        assertEquals(1, requests.get());
    }

    @Test
    void sameCurrencyWorksImmediatelyWithoutProviderOrWorker() {
        create((base, quote) -> { throw new AssertionError("Same currency must not contact the API"); });
        controller.convert(new ConversionInput("10.125", Currency.EUR, Currency.EUR));
        assertEquals(SUCCESS, state().status());
        assertEquals(new BigDecimal("10.13"), state().result().convertedAmount());
        assertTrue(state().result().rateDate().isEmpty());
        assertTrue(worker.tasks.isEmpty());
    }

    @Test
    void validatesInputWithoutSchedulingNetworkWork() {
        create();
        controller.convert(new ConversionInput("3,5", Currency.EUR, Currency.USD));
        assertEquals(ERROR, state().status());
        assertTrue(state().message().contains("decimal point"));
        assertTrue(worker.tasks.isEmpty());
    }

    @Test
    void missingSelectionHasUsefulFeedback() {
        create();
        controller.convert(new ConversionInput("10", null, Currency.USD));
        assertEquals(ERROR, state().status());
        assertEquals("Choose both currencies.", state().message());
        assertTrue(worker.tasks.isEmpty());
    }

    @Test
    void changingInputClearsAnExistingResult() {
        create();
        controller.convert(input);
        finish();
        controller.inputChanged(new ConversionInput("11", Currency.EUR, Currency.USD));
        assertEquals(IDLE, state().status());
        assertNull(state().result());
    }

    @Test
    void completionQueuedBeforeInputChangeCannotRestoreStaleData() {
        create();
        controller.convert(input);
        worker.runNext();
        controller.inputChanged(input.swapped());
        uiQueue.removeFirst().run();
        assertEquals(IDLE, state().status());
        assertNull(state().result());
    }

    @Test
    void anOlderResponseCannotOverwriteANewerConversion() {
        create();
        controller.convert(input);
        worker.runNext();
        controller.convert(new ConversionInput("20", Currency.EUR, Currency.USD));
        worker.runNext();
        uiQueue.removeLast().run();
        assertEquals(new BigDecimal("25.00"), state().result().convertedAmount());
        uiQueue.removeFirst().run();
        assertEquals(new BigDecimal("25.00"), state().result().convertedAmount());
    }

    @Test
    void failedRequestCanBeRetried() {
        create((base, quote) -> {
            if (requests.incrementAndGet() == 1) {
                throw new ExchangeRateException("Check your connection and try again.");
            }
            return new ExchangeRate(base, quote, new BigDecimal("1.25"), LocalDate.of(2026, 10, 20));
        });
        controller.convert(input);
        finish();
        assertEquals(ERROR, state().status());
        assertTrue(state().message().contains("connection"));
        controller.convert(input);
        finish();
        assertEquals(SUCCESS, state().status());
        assertEquals(2, requests.get());
    }

    @Test
    void rejectsProviderResultForTheWrongPair() {
        create((base, quote) -> new ExchangeRate(Currency.GBP, quote, BigDecimal.ONE, LocalDate.of(2026, 10, 20)));
        controller.convert(input);
        finish();
        assertEquals(ERROR, state().status());
        assertNull(state().result());
    }

    @Test
    void swapPreservesAmountAndReversesCurrenciesWithoutReusingResult() {
        ConversionInput original = new ConversionInput("42.1250", Currency.EUR, Currency.JPY);
        assertEquals(new ConversionInput("42.1250", Currency.JPY, Currency.EUR), original.swapped());
        assertEquals(original, original.swapped().swapped());
    }

    @Test
    void closeShutsDownWorkerAndDiscardsQueuedResults() {
        create();
        controller.convert(input);
        worker.runNext();
        int count = states.size();
        controller.close();
        uiQueue.removeFirst().run();
        controller.convert(input);
        controller.inputChanged(input.swapped());
        assertTrue(worker.isShutdown());
        assertEquals(count, states.size());
    }

    @Test
    void interruptedWorkRestoresTheInterruptFlag() {
        create((base, quote) -> { throw new InterruptedException("cancelled"); });
        controller.convert(input);
        try {
            worker.runNext();
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        uiQueue.removeFirst().run();
        assertEquals(ERROR, state().status());
    }

    @Test
    void unavailableExecutorProducesAnErrorInsteadOfStayingLoading() {
        create();
        worker.shutdown();
        controller.convert(input);
        assertEquals(ERROR, state().status());
    }

    private static final class ManualExecutor extends AbstractExecutorService {
        private final List<Runnable> tasks = new ArrayList<>();
        private boolean shutdown;

        void runNext() {
            tasks.removeFirst().run();
        }

        @Override public void shutdown() { shutdown = true; }
        @Override public List<Runnable> shutdownNow() {
            shutdown = true;
            List<Runnable> remaining = List.copyOf(tasks);
            tasks.clear();
            return remaining;
        }
        @Override public boolean isShutdown() { return shutdown; }
        @Override public boolean isTerminated() { return shutdown && tasks.isEmpty(); }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return isTerminated(); }
        @Override public void execute(Runnable task) {
            if (shutdown) {
                throw new RejectedExecutionException();
            }
            tasks.add(task);
        }
    }
}
