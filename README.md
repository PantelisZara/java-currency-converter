# Java Currency Converter

A JavaFX desktop currency converter built by a five-student university team.

**Project Lead and repository owner:** [PantelisZara](https://github.com/PantelisZara)  
**Submission deadline:** October 29, 2026, 18:00 Europe/Athens.

## Features

- Convert positive amounts between EUR, USD, GBP, CHF and JPY using the latest available reference rate returned by the Frankfurter v2 API.
- Show the converted amount, exchange rate and rate date.
- Swap the source and target currencies.
- Convert a currency to itself without making a network request.
- Validate amounts and display loading, success and actionable error states.
- Run network requests away from the JavaFX application thread and ignore results from outdated requests.
- Use `BigDecimal` arithmetic and `HALF_UP` rounding to the target currency's configured precision: two decimal places for EUR, USD, GBP and CHF; zero for JPY.

Amounts must be greater than zero, use a decimal point, contain no more than 12 integer digits and have no more than 4 decimal places. If a request fails, check the connection and try **Convert currency** again.

Frankfurter provides reference exchange rates, not live trading quotes. Conversion requires internet access.

## Technologies

| Technology | Version / purpose |
| --- | --- |
| Java | JDK 21 LTS |
| JavaFX | 21.0.13 |
| Maven | 3.9.16 through Maven Wrapper |
| Maven Wrapper | 3.3.4 |
| JUnit Jupiter | 5.14.4 |
| Gson | 2.14.0 |
| Java HttpClient | HTTP requests and timeouts |
| Frankfurter API | v2 reference rates |

## Build, test and run from source

Install a JDK 21 and set `JAVA_HOME`. The first Maven Wrapper run requires internet access to download Maven and project dependencies. A graphical desktop session is required to launch the JavaFX application.

### Linux

```sh
./mvnw --version
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw javafx:run
```

### Windows 11 (PowerShell)

```powershell
.\mvnw.cmd --version
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
.\mvnw.cmd javafx:run
```

The integration test suite contains 83 JUnit tests: 27 API, 42 domain and 14 controller tests. GitHub Actions runs the Maven build and tests on Ubuntu and Windows. CI does not by itself verify the visual desktop experience or the packaged Windows application.

## Build a portable Windows x64 distribution

Build the Windows package on Windows 11 x64 with a Windows x64 JDK 21. Cross-packaging the Windows application from Linux is not supported.

```powershell
$env:JAVA_HOME = "C:\Path\To\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\scripts\package-windows.ps1
```

The script runs Maven verification, checks that the required Windows JavaFX libraries are present, creates a `jpackage` app image, compresses it to a ZIP and writes a SHA-256 checksum. The default output is:

- `target\windows-dist\CurrencyConverter\CurrencyConverter.exe`
- `target\windows-dist\CurrencyConverter-windows-x64.zip`
- `target\windows-dist\CurrencyConverter-windows-x64.zip.sha256`

Extract the ZIP and launch `CurrencyConverter\CurrencyConverter.exe`. The packaged app image includes its Java runtime, so a separate JDK and Maven installation should not be needed to launch it; internet access is still required for exchange rates. The package is a portable app image, not a Windows installer.

A team member is responsible for the Windows 11 package and clean-machine smoke test. Record the tested source SHA, JDK/OS details, archive checksum and actual outcomes before claiming that the packaged build has been verified. If Windows-specific issues appear, fix them and repeat the checks.

## Architecture and team workflow

- `api`: Frankfurter requests, response validation, timeouts and API errors.
- `domain`: currency and rate models, amount validation, conversion and rounding.
- `ui`: JavaFX view, input state, asynchronous controller and user feedback.
- `CurrencyConverterApp`: wires the components together and closes background work and the HTTP client during shutdown.
- `Launcher`: entry point for `jpackage`.

See [Architecture](docs/ARCHITECTURE.md), [Team and contribution records](docs/TEAM.md), [Verification record](docs/VERIFICATION.md) and [Contributing](CONTRIBUTING.md).

Work is tracked through [GitHub Issues](https://github.com/PantelisZara/java-currency-converter/issues) and reviewed pull requests. Contribution records identify the actual GitHub PR authors; handoff packages or AI-assisted changes alone do not prove independent authorship. Follow the course's teamwork and AI-use rules.

## Known limitations

- The application requires the Frankfurter service and an internet connection for different-currency conversions.
- The Windows app-image build and clean-machine run must be performed on Windows. The current CI workflow tests compilation and unit tests, not GUI behavior.
- A Linux `jpackage` smoke image launched in the local OpenJDK 21.0.12.1 environment but crashed during native launcher shutdown; the Maven JavaFX launcher exited normally. The project targets the portable Windows x64 package, not a Linux native distribution.
- The ZIP is generated locally by the packaging script and is not committed to the repository.
