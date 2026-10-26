package com.github.panteliszara.currencyconverter.ui;

import com.github.panteliszara.currencyconverter.domain.ConversionResult;
import com.github.panteliszara.currencyconverter.domain.Currency;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Consumer;

public final class ConverterView {
    private final TextField amount = new TextField("100.00");
    private final ComboBox<Currency> source = new ComboBox<>();
    private final ComboBox<Currency> target = new ComboBox<>();
    private final Button swap = new Button("⇄  Swap");
    private final Button convert = new Button("Convert currency");
    private final TextArea result = new TextArea("—");
    private final Label resultContext = new Label("Your next conversion will appear here.");
    private final Label rate = new Label("Rate: —");
    private final Label date = new Label("Rate date: —");
    private final Label feedback = new Label();
    private final ProgressIndicator progress = new ProgressIndicator();
    private final ScrollPane root;
    private Consumer<ConversionInput> inputChanged = ignored -> { };
    private boolean settingInput;

    public ConverterView() {
        amount.setId("amount");
        amount.setPromptText("e.g. 125.50");
        amount.setAccessibleHelp("Positive decimal amount; up to 12 integer digits and 4 decimal places.");
        source.setId("source-currency");
        target.setId("target-currency");
        source.getItems().setAll(Currency.values());
        target.getItems().setAll(Currency.values());
        source.setValue(Currency.EUR);
        target.setValue(Currency.USD);
        source.setMaxWidth(Double.MAX_VALUE);
        target.setMaxWidth(Double.MAX_VALUE);
        source.setMinWidth(0);
        target.setMinWidth(0);
        swap.setId("swap");
        swap.setAccessibleText("Swap source and target currencies");
        convert.setId("convert");
        convert.setDefaultButton(true);
        convert.setMaxWidth(Double.MAX_VALUE);
        convert.getStyleClass().add("primary-button");
        amount.setOnAction(event -> {
            convert.fire();
            event.consume();
        });

        Label eyebrow = label("DAILY EXCHANGE RATES", "eyebrow");
        Label title = label("Currency Converter", "title");
        Label subtitle = label("Five currencies. One straightforward conversion.", "subtitle");
        VBox header = new VBox(10, eyebrow, title, subtitle);
        header.getStyleClass().add("header");

        Label amountLabel = label("Amount", "field-label");
        amountLabel.setLabelFor(amount);
        VBox amountField = new VBox(9, amountLabel, amount,
                label("Use a decimal point · up to 4 decimal places", "hint"));
        Label sourceLabel = label("From", "field-label");
        sourceLabel.setLabelFor(source);
        Label targetLabel = label("To", "field-label");
        targetLabel.setLabelFor(target);
        VBox sourceField = new VBox(9, sourceLabel, source);
        VBox targetField = new VBox(9, targetLabel, target);
        VBox swapField = new VBox(swap);
        swapField.setAlignment(Pos.BOTTOM_CENTER);
        GridPane currencies = new GridPane();
        currencies.setHgap(12);
        ColumnConstraints first = new ColumnConstraints();
        first.setHgrow(Priority.ALWAYS);
        first.setMinWidth(0);
        ColumnConstraints middle = new ColumnConstraints();
        middle.setMinWidth(94);
        middle.setPrefWidth(94);
        middle.setMaxWidth(94);
        ColumnConstraints last = new ColumnConstraints();
        last.setHgrow(Priority.ALWAYS);
        last.setMinWidth(0);
        currencies.getColumnConstraints().addAll(first, middle, last);
        currencies.addRow(0, sourceField, swapField, targetField);

        feedback.setId("feedback");
        feedback.setWrapText(true);
        feedback.setMaxWidth(Double.MAX_VALUE);
        feedback.setMinHeight(24);
        feedback.getStyleClass().add("feedback");
        progress.setId("loading");
        progress.setMaxSize(20, 20);
        progress.setMinSize(20, 20);
        progress.setAccessibleText("Retrieving exchange rate");
        HBox status = new HBox(10, progress, feedback);
        status.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(feedback, Priority.ALWAYS);
        VBox form = new VBox(18, amountField, currencies, convert, status);
        form.getStyleClass().add("card");

        result.setId("converted-amount");
        result.setEditable(false);
        result.setWrapText(true);
        result.setPrefRowCount(2);
        result.setMinHeight(80);
        result.setPrefHeight(80);
        result.setAccessibleText("Converted amount");
        result.getStyleClass().add("result-amount");
        resultContext.getStyleClass().add("subtitle");
        resultContext.setWrapText(true);
        rate.setId("exchange-rate");
        date.setId("rate-date");
        rate.setWrapText(true);
        date.setWrapText(true);
        rate.getStyleClass().add("rate");
        date.getStyleClass().add("hint");
        VBox resultCard = new VBox(10, label("CONVERTED AMOUNT", "eyebrow"), result,
                resultContext, rate, date);
        resultCard.getStyleClass().addAll("card", "result-card");
        Label footer = label("Reference rates from Frankfurter · not live trading quotes", "hint");
        VBox page = new VBox(20, header, form, resultCard, footer);
        page.getStyleClass().add("page");
        page.setMaxWidth(900);
        VBox centered = new VBox(page);
        centered.setAlignment(Pos.TOP_CENTER);
        root = new ScrollPane(centered);
        root.setId("converter-root");
        root.setFitToWidth(true);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.getStylesheets().add(Objects.requireNonNull(
                ConverterView.class.getResource("converter.css")).toExternalForm());

        amount.textProperty().addListener((observable, oldValue, newValue) -> changed());
        source.valueProperty().addListener((observable, oldValue, newValue) -> changed());
        target.valueProperty().addListener((observable, oldValue, newValue) -> changed());
        render(new ConversionState(ConversionState.Status.IDLE, "Ready when you are.", null));
    }

    public Parent root() {
        return root;
    }

    public ConversionInput input() {
        return new ConversionInput(amount.getText(), source.getValue(), target.getValue());
    }

    public void setInput(ConversionInput input) {
        settingInput = true;
        try {
            amount.setText(input.amount());
            source.setValue(input.source());
            target.setValue(input.target());
        } finally {
            settingInput = false;
        }
        changed();
    }

    public void onInputChanged(Consumer<ConversionInput> handler) {
        inputChanged = Objects.requireNonNull(handler);
    }

    public void onConvert(Runnable action) {
        convert.setOnAction(event -> action.run());
    }

    public void onSwap(Runnable action) {
        swap.setOnAction(event -> action.run());
    }

    private void changed() {
        if (!settingInput) {
            inputChanged.accept(input());
        }
    }

    public void render(ConversionState state) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("View updates must run on the JavaFX application thread.");
        }
        boolean loading = state.status() == ConversionState.Status.LOADING;
        progress.setVisible(loading);
        progress.setManaged(loading);
        convert.setDisable(loading);
        convert.setText(loading ? "Converting…" : "Convert currency");
        feedback.setText(state.message());
        feedback.getStyleClass().remove("error");
        if (state.status() == ConversionState.Status.ERROR) {
            feedback.getStyleClass().add("error");
        }
        if (state.status() == ConversionState.Status.SUCCESS) {
            ConversionResult value = state.result();
            result.setText(value.convertedAmount().toPlainString() + " " + value.target().name());
            resultContext.setText(value.amount().toPlainString() + " " + value.source().name()
                    + " → " + value.target().name());
            rate.setText("1 " + value.source().name() + " = " + value.rate().stripTrailingZeros().toPlainString()
                    + " " + value.target().name());
            date.setText(value.rateDate().map(day -> "Rate date: " + day)
                    .orElse("Rate date: Not applicable (same currency; no network request)"));
        } else {
            result.setText("—");
            resultContext.setText("Your next conversion will appear here.");
            rate.setText("Rate: —");
            date.setText("Rate date: —");
        }
    }

    private static Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        label.setWrapText(true);
        return label;
    }
}
