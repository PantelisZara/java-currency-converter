package com.github.panteliszara.currencyconverter.api;

import com.github.panteliszara.currencyconverter.domain.Currency;
import com.github.panteliszara.currencyconverter.domain.ExchangeRate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public final class FrankfurterClient implements ExchangeRateProvider {
    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Gson JSON = new GsonBuilder().setStrictness(Strictness.STRICT).create();
    private final HttpClient client;
    private final URI endpoint;
    private final Duration timeout;

    public FrankfurterClient(HttpClient client) {
        this(client, URI.create("https://api.frankfurter.dev/v2/rate/"), REQUEST_TIMEOUT);
    }

    FrankfurterClient(HttpClient client, URI endpoint, Duration timeout) {
        this.client = Objects.requireNonNull(client);
        this.endpoint = Objects.requireNonNull(endpoint);
        this.timeout = Objects.requireNonNull(timeout);
    }

    @Override
    public ExchangeRate fetch(Currency base, Currency quote) throws ExchangeRateException, InterruptedException {
        Objects.requireNonNull(base);
        Objects.requireNonNull(quote);
        URI uri = endpoint.resolve(base.name().toLowerCase(Locale.ROOT) + "/"
                + quote.name().toLowerCase(Locale.ROOT));
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(timeout)
                .header("Accept", "application/json")
                .header("User-Agent", "JavaCurrencyConverter/0.1.0")
                .GET().build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new ExchangeRateException("The rate request timed out. Please try again.", e);
        } catch (IOException e) {
            throw new ExchangeRateException(
                    "Cannot reach the rate service. Check your connection and try again.", e);
        }
        if (response.statusCode() != 200) {
            String message = response.statusCode() == 429
                    ? "The rate service is busy. Please try again shortly."
                    : "The rate service returned HTTP " + response.statusCode() + ". Please try again.";
            throw new ExchangeRateException(message);
        }
        try {
            JsonObject data = JSON.fromJson(response.body(), JsonObject.class);
            if (data == null || !base.name().equals(string(data, "base"))
                    || !quote.name().equals(string(data, "quote"))) {
                throw new IllegalArgumentException("Unexpected currency pair");
            }
            JsonElement value = data.get("rate");
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException("A numeric rate is required");
            }
            BigDecimal rate = value.getAsBigDecimal();
            LocalDate date = LocalDate.parse(string(data, "date"));
            return new ExchangeRate(base, quote, rate, date);
        } catch (RuntimeException e) {
            throw new ExchangeRateException("The rate service returned invalid data. Please try again.", e);
        }
    }

    private static String string(JsonObject data, String field) {
        JsonElement value = data.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("A string is required for " + field);
        }
        return value.getAsString();
    }
}
