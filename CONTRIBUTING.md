# Contributing

## Issue-based development

Read the Issue, dependencies and acceptance criteria before implementation.
Confirm ownership with the Project Lead. Keep each change focused on one Issue
and agree on shared contracts before depending on another workstream.

## Fork-based workflow

The four additional students contribute through forks of this public repository.
Collaborator invitations are not required. Fork
[PantelisZara/java-currency-converter](https://github.com/PantelisZara/java-currency-converter)
to your own GitHub account, then clone your fork and add the upstream remote:

```sh
git clone https://github.com/<your-github-username>/java-currency-converter.git
cd java-currency-converter
git remote add upstream https://github.com/PantelisZara/java-currency-converter.git
```

Replace angle-bracket placeholders before running commands. `origin` must point
to your fork; `upstream` must point to PantelisZara's repository. From a clean
checkout, synchronize your fork's `main` and create a feature branch:

```sh
git switch main
git fetch upstream
git merge --ff-only upstream/main
git push origin main
git switch -c issue-<number>-<topic>
```

Replace the branch-name placeholders with the real Issue number and topic.
Use short-lived branches; do not maintain permanent workstream branches.
Preserve unexpected local changes before switching or integrating work.
If a fast-forward fails, stop and review the divergence with the Project Lead.

Receive the assigned local handoff package from the Project Lead. Read its scope,
dependencies and integration order before applying a patch. Review and understand
the supplied code, run its relevant checks, and meaningfully improve your assigned
work. Record actual changes and verification; receiving a package does not establish
independent authorship. Keep other workstreams out of your PR.

The Project Lead may create WS1 branches directly in the upstream repository.

## Implementation and testing

- Keep JavaFX controls and event handling in `ui`; keep future conversion rules
  and HTTP integration in their separate areas.
- Do not block the JavaFX application thread with HTTP requests or waiting for
  background work.
- Add meaningful JUnit tests for new behavior. Tests must use deterministic
  data and must not depend on the live rate service in ordinary CI.
- Run `./mvnw --batch-mode --no-transfer-progress clean verify` on Linux or
  `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify` on Windows.
- For GUI changes, launch the application and record relevant keyboard, resize,
  error-state and shutdown checks. Windows-specific claims require Windows checks.
- Review `git diff --check`, the full diff and `git status` before committing.
  Never commit credentials, caches, IDE settings, logs or generated build outputs.

## Commits, PRs and review

Use professional English and Conventional Commits, such as
`feat: validate conversion amount` or `test: cover rounding boundaries`.
Commit under your own identity and describe the actual change.

Push your feature branch to your fork with
`git push -u origin issue-<number>-<topic>`. Open a PR from that branch to
`PantelisZara/java-currency-converter:main` using the template. Link the Issue,
explain behavior changes, report checks actually run, and identify skipped checks
and known limitations. For partial delivery, use `Refs #<issue-number>` rather
than a closing keyword; close an Issue only when all acceptance criteria are met.
Request review from another team member and address feedback before integration.
PantelisZara reviews, coordinates and merges PRs once review and CI are complete.

An Issue is complete when its acceptance criteria, relevant tests, required
manual checks and documentation are satisfied. A passing build alone does not
verify GUI operation or Windows packaging.

## Contribution records

Record actual implementation, tests, reviews, integration and pairing work.
Confirm contributor identities and credit with the people involved. Do not
attribute work to another student, invent activity, or mark assignments complete
before their acceptance criteria are met. Follow the university's AI-use and
submission rules, including required disclosure of assistance and accurate human
contribution records. Commit only your actual work under your own identity.
