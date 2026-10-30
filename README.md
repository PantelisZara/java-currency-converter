# Java Currency Converter

A JavaFX university assignment for learning Git and teamwork.
Converts EUR, USD, GBP, CHF and JPY using Frankfurter daily rates.
Supports amount validation, Swap and same-currency conversion offline.

Requires JDK 21, `JAVA_HOME` and a graphical desktop. The Maven Wrapper downloads
Maven and JavaFX; the first build needs internet. No separate SDK is needed.

Linux:

```sh
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw javafx:run
./mvnw javadoc:javadoc
```

Windows PowerShell:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
.\mvnw.cmd javafx:run
.\mvnw.cmd javadoc:javadoc
```

For a bundled Windows x64 app, run `.\scripts\package-windows.ps1` with Windows x64 JDK 21.
Extract `target/windows-dist/CurrencyConverter-windows-x64.zip` and run
`CurrencyConverter/CurrencyConverter.exe`; keep the whole folder. Java is included.

## Team

- [PantelisZara](https://github.com/PantelisZara) — lead, build and integration ([#8](https://github.com/PantelisZara/java-currency-converter/pull/8), [#13](https://github.com/PantelisZara/java-currency-converter/pull/13)).
- [Nikolakys23](https://github.com/Nikolakys23) — API ([#10](https://github.com/PantelisZara/java-currency-converter/pull/10)).
- [thanosfest](https://github.com/thanosfest) — domain and rounding ([#9](https://github.com/PantelisZara/java-currency-converter/pull/9)).
- [LympPs1](https://github.com/LympPs1) — JavaFX view ([#12](https://github.com/PantelisZara/java-currency-converter/pull/12)).
- [fotistsalis7-ux](https://github.com/fotistsalis7-ux) — controller ([#11](https://github.com/PantelisZara/java-currency-converter/pull/11)).


