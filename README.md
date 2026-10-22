# Java Currency Converter

A university team project for a desktop currency converter, owned and led
by [PantelisZara](https://github.com/PantelisZara). The team consists of five students.

**Deadline: October 29, 2026, 18:00 Europe/Athens.**

## Implementation status

The application currently opens a resizable JavaFX window with a project title
and an implementation-status message. Currency conversion and API access are
not implemented.

The Maven build, wrapper, JUnit dependency and Linux/Windows CI workflow are in
place. There are currently **zero automated tests**: the test phase has no test
sources to execute. Meaningful tests will be added alongside application behavior.

## Technologies

| Technology | Version / purpose |
| --- | --- |
| Java | JDK 21 LTS |
| JavaFX | 21.0.13 |
| Maven | 3.9.16 through Maven Wrapper 3.3.4 |
| JUnit Jupiter | 5.14.4, test scope |
| Gson | 2.14.0, prepared for future API response parsing |
| JavaFX Maven Plugin | 0.0.8, desktop launch |
| Java HttpClient | Built into Java 21; planned HTTP client |
| Frankfurter API | v2; planned exchange-rate provider |

The development environment is Arch Linux. The target desktop environment is
Windows 11. No separate Maven installation or JavaFX SDK is required.

## Build and run

Install JDK 21 and set `JAVA_HOME` to its installation directory. The first build
requires internet access to download Maven and dependencies. The wrapper pins
Maven and verifies the downloaded distribution's SHA-256 checksum.

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

A graphical desktop session is required for `javafx:run`. Confirm the title and
status message are visible, resize the window, and close it to check shutdown.

The optional `app-image` Maven profile copies runtime dependencies to
`target/package-input` during packaging. It prepares inputs for future packaging;
it does not create a Java runtime, Windows launcher or installer. The bootstrap
application does not use Gson or call the API yet.

## Planned functionality

- Source and target currency selection, amount input, and result display.
- Frankfurter v2 rate retrieval, including rate-date metadata and useful errors.
- Decimal conversion with an explicit precision and rounding policy.
- Input validation, currency swapping, loading states, and retry behavior.
- Background requests that keep the JavaFX application thread responsive.
- A Windows 11 distribution and final integration verification.

The planned endpoint is <https://api.frankfurter.dev/v2/rate/EUR/USD>.
Its contract will be verified during API integration; the current application
does not contact it.

## Team workflow

Work is tracked in [GitHub Issues](https://github.com/PantelisZara/java-currency-converter/issues).
Agree on ownership and contracts before implementation. The four additional
students fork this public repository and create short-lived branches in their
forks. Each student receives their assigned handoff package, reviews, understands,
tests and meaningfully improves the code, then commits actual work under their
own identity and opens a focused PR to `PantelisZara/java-currency-converter:main`.
No collaborator invitations are required. Another team member reviews each PR;
PantelisZara reviews, coordinates and merges after review and CI.

See [Contributing](CONTRIBUTING.md), [Architecture](docs/ARCHITECTURE.md), and
[Team](docs/TEAM.md) for development rules and responsibilities.

## Known limitations

- No rate client, conversion rules, controller, currency selectors, validation,
  swap behavior, or custom styling exists yet.
- JUnit is configured, but there are no automated tests or coverage claims.
- CI builds on Ubuntu and Windows; it does not launch a desktop or verify
  Windows 11 usability.
- The JAR in `target/` is not a self-contained desktop distribution. Packaging
  and clean Windows 11 verification are separate delivery tasks.
