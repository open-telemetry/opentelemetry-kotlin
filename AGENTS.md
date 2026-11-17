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
| `instrumentation/*` | Instrumentation for third-party libraries |
| `testing` / `test-fakes` | Test utilities |
| `semconv` | Semantic conventions |
| `examples` | Example apps |

Targets: JVM, Android (API 21+), iOS, JS.

## Workflow

- **Build & verify**: `./gradlew verify` — runs tests + static analysis (Detekt, API checks). Use it for most changesets.
  `./gradlew build` additionally assembles everything and runs `gradle-integration-test`, which needs
  `./gradlew publishToMavenLocal -PsnapshotPublish=true` first (see `CONTRIBUTING.md`).
- **Android tests**: skip running locally, but verify compilation with `./gradlew assembleAndroidTest`
- **API changes**: run `./gradlew updateKotlinAbi` to update the ABI dump files (JVM, Android and klib) before opening a PR.
- **New modules**: decide whether the module is a public entrypoint. That means consumers depend on it directly, or
  another public module exports it with `api(...)`. If it is, set `io.opentelemetry.kotlin.containsPublicApi=true` in its
  `gradle.properties` and run `./gradlew updateKotlinAbi`, which turns on explicit API mode and binary compatibility checks.
  Internal modules that other modules only pull in with `implementation(...)` leave the property unset (the default is
  `false`). Before a public module exports an internal one with `api(...)`, opt the internal module in first.
- **PR size**: keep diffs under 500 lines.
- **AI contributions**: all AI-generated code must be manually reviewed before committing.
- **OTel spec** Search the OpenTelemetry specification when relevant and keep changes compliant

## Error handling

This SDK is embedded in other people's apps. **Telemetry must never destabilize the host.** Follow the
[OTel error-handling spec](https://opentelemetry.io/docs/specs/otel/error-handling/) and these general principles:

- Never crash the host.** Nothing may escape a public API method, a callback the SDK invokes,
  or coroutines
- Invalid input should degrade to a safe default
- Fast fail should only happen when the SDK has been misconfigured at initialization
- All user-supplied code should be assumed as hostile
- Errors should be routed to SdkErrorHandler rather than swallowing (at an appropriate cadence)

## AI agent guidelines

- Do NOT inspect the gradle cache or decompile JARs.
- Avoid module-specific tasks during verification unless necessary. E.g. `./gradlew detekt` rather than `./gradlew :module:detekt`.
- Select commands appropriately during verification to avoid unnecessary permission prompts.
- Ask lots of clarifying questions. Interview the programmer often when gathering requirements.
- Do NOT work on issues that have the `contribfest` label unless your programmer/operator
  has explicitly indicated that they are live, in-person at a Kubecon ContribFest event.
