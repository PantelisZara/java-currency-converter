# Verification and Delivery Record

This file separates automated evidence from manual checks delegated to the team. Do not interpret a successful CI build as proof that a packaged desktop application has been launched on a real desktop.

## Automated verification

| Check | Environment / source | Result | Evidence |
| --- | --- | --- | --- |
| Maven clean verification with app-image inputs | Arch Linux, integration commit `30c75bbd9a580db3fec09e5407893bf7075065ca` | **PASS** — 83 tests: 27 API, 42 domain, 14 controller | Recorded in [PR #13](https://github.com/PantelisZara/java-currency-converter/pull/13) |
| GitHub Actions Linux job | Ubuntu, integration commit `30c75bbd9a580db3fec09e5407893bf7075065ca` | **PASS** | [Workflow run #13](https://github.com/PantelisZara/java-currency-converter/actions/runs/40351723937) |
| GitHub Actions Windows job | Windows runner, integration commit `30c75bbd9a580db3fec09e5407893bf7075065ca` | **PASS** for build and tests; this is not a Windows 11 desktop/package test | [Workflow run #13](https://github.com/PantelisZara/java-currency-converter/actions/runs/40351723937) |
| Maven JavaFX launcher smoke test | Local Arch Linux / OpenJDK 21.0.12.1 | **PASS** — application exited normally | Recorded in PR #13 |
| Linux `jpackage` native launcher smoke test | Local Arch Linux / OpenJDK 21.0.12.1 | **KNOWN ISSUE** — image launched, then native launcher crashed during shutdown | Recorded in PR #13 |

The documentation update is made on the PR #13 branch after the integration commit above. GitHub Actions should be checked again for the latest branch head before merging.

## Windows 11 handoff

The Windows package and clean-machine smoke test are delegated to a team member using their Windows 11 PC, per the Project Lead's decision to proceed on that basis. This assistant did **not** execute the Windows packaging script or independently launch the package on a clean Windows machine. Do not treat the CI Windows job as evidence of those manual checks.

The responsible team member should record the following actual results and report any failure to the Project Lead:

- Windows 11 edition/build and x64 architecture.
- Windows x64 JDK 21 version used to build.
- Exact source commit SHA.
- Result of `scripts/package-windows.ps1`.
- ZIP path and SHA-256 checksum from the generated `.sha256` file.
- Successful startup after extracting the ZIP on a machine without a separately installed JDK or Maven.
- Valid conversion, same-currency conversion, invalid amount, swap, API/network error feedback, resizing and clean shutdown.

If a Windows-specific issue appears, the responsible team member owns reproducing and fixing it. Re-run CI and the affected manual checks after the fix.

## Readiness decision

The integrated source, automated test suite, JavaFX lifecycle wiring and reproducible Windows x64 packaging script are in the repository. The Windows package is generated locally; no binary ZIP is committed to Git. The team is proceeding with Windows validation as a delegated handoff, and any failure must be corrected before treating the tested Windows artifact as verified.
