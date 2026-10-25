package com.github.panteliszara.currencyconverter.api;

import com.github.panteliszara.currencyconverter.domain.Currency;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FrankfurterClientTest {
    private static final String VALID = """
            {"date":"2026-10-20","base":"EUR","quote":"USD","rate":1.23456789012345}
            """;
    private HttpServer server;
    private ExecutorService serverThreads;
    private HttpClient http;
    private URI endpoint;
    private FrankfurterClient client;
    private final AtomicReference<String> body = new AtomicReference<>(VALID);
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicLong delay = new AtomicLong();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> accept = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        serverThreads = Executors.newCachedThreadPool(Thread.ofPlatform().daemon().factory());
        server.setExecutor(serverThreads);
        server.createContext("/rate/", exchange -> {
            try {
                path.set(exchange.getRequestURI().getPath());
                accept.set(exchange.getRequestHeaders().getFirst("Accept"));
                try {
                    Thread.sleep(delay.get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(status.get(), bytes.length);
                exchange.getResponseBody().write(bytes);
            } finally {
                exchange.close();
            }
        });
        server.start();
        endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/rate/");
        http = HttpClient.newBuilder().connectTimeout(FrankfurterClient.CONNECT_TIMEOUT).build();
        client = new FrankfurterClient(http, endpoint, FrankfurterClient.REQUEST_TIMEOUT);
    }

    @AfterEach
    void stopResources() {
        if (http != null) {
            http.shutdownNow();
        }
        if (server != null) {
            server.stop(0);
        }
        if (serverThreads != null) {
            serverThreads.shutdownNow();
        }
    }

    @Test
    void readsExactDecimalRateAndActualDate() throws Exception {
        var rate = client.fetch(Currency.EUR, Currency.USD);
        assertEquals(new BigDecimal("1.23456789012345"), rate.value());
        assertEquals(LocalDate.of(2026, 10, 20), rate.date());
        assertEquals(Currency.EUR, rate.base());
        assertEquals(Currency.USD, rate.quote());
        assertEquals("/rate/eur/usd", path.get());
        assertEquals("application/json", accept.get());
    }

    @Test
    void toleratesAdditionalResponseMetadata() throws Exception {
        body.set(VALID.strip().replace("}", ",\"providers\":[\"ecb\"]}"));
        assertEquals(new BigDecimal("1.23456789012345"), client.fetch(Currency.EUR, Currency.USD).value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "not JSON", "null", "[]", "{}", "{\"rate\":1}",
            "{\"date\":\"2026-10-20\",\"base\":\"GBP\",\"quote\":\"USD\",\"rate\":1}",
            "{\"date\":\"2026-10-20\",\"base\":\"EUR\",\"quote\":\"JPY\",\"rate\":1}",
            "{\"date\":\"2026-02-30\",\"base\":\"EUR\",\"quote\":\"USD\",\"rate\":1}",
            "{\"date\":null,\"base\":\"EUR\",\"quote\":\"USD\",\"rate\":1}",
            "{\"date\":\"2026-10-20\",\"base\":\"EUR\",\"quote\":\"USD\"}",
            "{\"date\":\"2026-10-20\",\"base\":\"EUR\",\"quote\":\"USD\",\"rate\":null}",
            "{\"date\":\"2026-10-20\",\"base\":\"EUR\",\"quote\":\"USD\",\"rate\":0}",
            "{\"date\":\"2026-10-20\",\"base\":\"EUR\",\"quote\":\"USD\",\"rate\":-1}",
            "{\"date\":\"2026-10-20\",\"base\":\"EUR\",\"quote\":\"USD\",\"rate\":\"1.2\"}",
            "{date:'2026-10-20',base:'EUR',quote:'USD',rate:1}",
            "{\"date\":\"2026-10-20\",\"base\":\"EUR\",\"quote\":\"USD\",\"rate\":NaN}",
            "{} {}"
    })
    void rejectsMalformedMissingAndUnexpectedData(String response) {
        body.set(response);
        ExchangeRateException error = assertThrows(ExchangeRateException.class,
                () -> client.fetch(Currency.EUR, Currency.USD));
        assertTrue(error.getMessage().contains("invalid data"));
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 404, 429, 503})
    void handlesHttpErrorsBeforeParsingTheBody(int code) {
        status.set(code);
        body.set("service unavailable");
        ExchangeRateException error = assertThrows(ExchangeRateException.class,
                () -> client.fetch(Currency.EUR, Currency.USD));
        assertTrue(error.getMessage().contains(code == 429 ? "busy" : "HTTP " + code));
    }

    @Test
    void mapsRequestTimeoutToUsefulFeedback() {
        delay.set(1000);
        var shortTimeout = new FrankfurterClient(http, endpoint, Duration.ofMillis(150));
        ExchangeRateException error = assertThrows(ExchangeRateException.class,
                () -> shortTimeout.fetch(Currency.EUR, Currency.USD));
        assertTrue(error.getMessage().contains("timed out"));
    }

    @Test
    void handlesConnectionFailure() {
        server.stop(0);
        server = null;
        ExchangeRateException error = assertThrows(ExchangeRateException.class,
                () -> client.fetch(Currency.EUR, Currency.USD));
        assertTrue(error.getMessage().contains("Check your connection"));
    }

    @Test
    void propagatesInterruptionForCancellation() {
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedException.class, () -> client.fetch(Currency.EUR, Currency.USD));
        } finally {
            Thread.interrupted();
        }
    }
}
