package com.github.panteliszara.currencyconverter;

import com.github.panteliszara.currencyconverter.api.FrankfurterClient;
import com.github.panteliszara.currencyconverter.domain.ConversionService;
import com.github.panteliszara.currencyconverter.ui.ConversionController;
import com.github.panteliszara.currencyconverter.ui.ConverterView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

public final class CurrencyConverterApp extends Application {
    private ConversionController controller;
    private HttpClient httpClient;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Java Currency Converter");
        httpClient = HttpClient.newBuilder().connectTimeout(FrankfurterClient.CONNECT_TIMEOUT).build();
        var worker = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "exchange-rate-worker");
            thread.setDaemon(true);
            return thread;
        });
        ConverterView view = new ConverterView();
        controller = new ConversionController(new FrankfurterClient(httpClient), new ConversionService(),
                worker, Platform::runLater, view::render);
        view.onInputChanged(controller::inputChanged);
        view.onConvert(() -> controller.convert(view.input()));
        view.onSwap(() -> view.setInput(view.input().swapped()));
        double height = Math.min(880, Screen.getPrimary().getVisualBounds().getHeight() - 80);
        stage.setScene(new Scene(view.root(), 780, height));
        stage.setMinWidth(680);
        stage.setMinHeight(520);
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.close();
        }
        if (httpClient != null) {
            httpClient.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
