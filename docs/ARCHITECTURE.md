# Architecture

## Application structure

The project is a single, non-modular Maven application targeting Java 21 and JavaFX 21.0.13.

| Component | Responsibility |
| --- | --- |
| `CurrencyConverterApp` | Creates the HTTP client, background worker, API provider, conversion service, controller and JavaFX view; owns application startup and shutdown. |
| `Launcher` | Starts the JavaFX application through a dedicated entry point for `jpackage`. |
| `api/FrankfurterClient` | Requests and validates reference rates from Frankfurter v2; maps network, timeout, HTTP and malformed-response failures to `ExchangeRateException`. |
| `api/ExchangeRateProvider` | Small provider contract used by the controller and deterministic tests. |
| `domain/Currency` | Supported currencies and target precision: EUR/USD/GBP/CHF use two fraction digits; JPY uses zero. |
| `domain/ExchangeRate` | Validated base/quote rate, positive value and rate date. |
| `domain/ConversionService` | Parses and validates amounts, performs `BigDecimal` conversion and applies `HALF_UP` rounding. |
| `domain/ConversionResult` | Carries source amount, source/target currencies, converted amount, rate and optional rate date. |
| `ui/ConverterView` | JavaFX inputs, currency selectors, swap/convert actions, result display, loading state and feedback. |
| `ui/ConversionController` | Coordinates validation, same-currency conversion, background rate requests, state updates, cancellation and stale-result suppression. |

## Request and conversion flow

1. The view emits a `ConversionInput` when the amount or currency selections change.
2. The controller validates that both currencies are selected and that the amount is positive, has at most 12 integer digits and at most 4 decimal places.
3. If source and target currencies match, the domain service returns the amount at the target currency's precision without making an HTTP request.
4. Otherwise, the controller schedules the request on a dedicated worker so the JavaFX application thread remains responsive.
5. `FrankfurterClient` sends a GET request to `https://api.frankfurter.dev/v2/rate/{BASE}/{QUOTE}`. Connection timeout is 5 seconds and request timeout is 10 seconds. The response must match the requested pair and contain a numeric rate and a valid date.
6. The domain service multiplies the amount by the rate using `BigDecimal` and rounds to the target currency's fraction digits with `RoundingMode.HALF_UP`.
7. The controller dispatches the result to the JavaFX thread. Results from cancelled or superseded requests are ignored. The view displays the converted amount, rate date and status message.

The user can retry a failed request by initiating another conversion. No automatic retry loop is implemented. Frankfurter rates are reference rates, not live trading quotes.

## Lifecycle and error handling

The HTTP client uses a 5-second connection timeout, while each rate request has a 10-second timeout. Network errors, timeout, non-200 responses (including a specific busy-service message for HTTP 429), and invalid response data become user-facing errors. The controller cancels pending work when the input changes and shuts down its executor on close. The application calls controller shutdown and HTTP client shutdown from `Application.stop()`.

The API, domain and controller have deterministic JUnit tests. Unit tests should not depend on the live rate service. The current suite has 27 API, 42 domain and 14 controller tests.

## Build and Windows packaging

Use the Maven Wrapper for source builds and tests:

- Linux: `./mvnw --batch-mode --no-transfer-progress clean verify`
- Windows: `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify`

The `app-image` Maven profile copies runtime dependencies into `target/package-input`; by itself it does not create a distributable runtime image. Run `scripts/package-windows.ps1` on Windows 11 x64 with a Windows x64 JDK 21 to create a `jpackage` app image, ZIP archive and SHA-256 checksum. The script checks for required Windows JavaFX JARs and rejects Linux JavaFX JARs. The ZIP is generated locally and is not committed to Git.

Windows CI confirms compilation and unit tests, not desktop startup. A team member owns the Windows 11 package and clean-machine smoke test; see [Verification](VERIFICATION.md) for the recorded evidence and remaining manual checks.

## Known platform limitation

A Linux `jpackage` smoke image launched but crashed during native launcher shutdown in the local OpenJDK 21.0.12.1 environment. Launching through the Maven JavaFX plugin exited normally. This project targets the Windows x64 app image; the Linux native launcher issue is recorded rather than presented as a successful Linux package test.
