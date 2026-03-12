# AGENTS.md

This repo is a Kotlin Multiplatform (KMP) implementation of the [OpenTelemetry specification](https://opentelemetry.io/docs/specs/otel/). All relevant design decisions should align with that spec.

## Project structure

| Module | Purpose |
|--------|---------|
| `api` | Public API |
| `sdk` | Public API (for initializing the SDK) |
| `implementation` | KMP implementation of OpenTelemetry |
| `compat` | Facade of `api` that uses opentelemetry-java under the hood |
| `exporters-*` | OTLP, in-memory, etc. |
| `testing` / `test-fakes` | Test utilities |
| `semconv` | Semantic conventions |
| `examples` | Example apps |

Targets: JVM, Android (API 21+), iOS, JS.

## Workflow

- **Build & verify**: `./gradlew build` — runs tests + Detekt (static analysis).
- **Android tests**: skip running locally, but verify compilation with `./gradlew assembleAndroidTest`
- **API changes**: run `./gradlew apiDump` to update dump files before opening a PR.
- **PR size**: keep diffs under 500 lines.
- **AI contributions**: all AI-generated code must be manually reviewed before committing.
- **OTel spec** Search the OpenTelemetry specification when relevant and keep changes compliant
