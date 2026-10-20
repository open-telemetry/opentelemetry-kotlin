# Contributing to opentelemetry-kotlin

Please open an issue on GitHub with your bug report/feature request and somebody will get back to
you on whether it's something that is actively being worked on, or whether external contributions
would be accepted.

## Setting up development environment

1. Fork and clone the repository
2. Install the following prerequisites:
   1. JDK >=11 (OpenJDK 21 via https://sdkman.io/ is recommended)
   2. **Android SDK** — Android Studio bundles the SDK and is the easiest route: https://developer.android.com/studio.
      If installing the SDK manually, ensure the following packages are present:
      - `platforms;android-36`
      - `build-tools;36.0.0`
      - `platform-tools`
   3. **Xcode** (macOS only) — the full Xcode app is required, not just the command-line tools.
      Install from the Mac App Store, then run:
      ```bash
      sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
      sudo xcodebuild -license accept
      ```
      The supported Xcode version depends on the Kotlin version used in the project — check the
      [Kotlin Multiplatform compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html).
      If your Xcode version is newer than the supported range, iOS simulator tests will fail; either
      install a supported iOS platform runtime via **Xcode → Settings → Platforms**, or exclude the
      tests temporarily with `-x iosSimulatorArm64Test -x iosArm64Test`.
      tvOS simulator tests are skipped by default. To run them, install a tvOS platform runtime and
      pass `-Pio.opentelemetry.kotlin.enableTvosTests=true`.
   4. Android Studio or IntelliJ IDEA are recommended
3. Follow [Running the full build including the integration test](#running-the-full-build-including-the-integration-test)
   to confirm the project builds
4. Open an issue or update these docs if there was a step missing from these instructions!

### Verifying a changeset

`./gradlew verify` runs static analysis and tests for every module except `gradle-integration-test`.
This should be sufficient for most changesets.

### Running the full build including the integration test

The `gradle-integration-test` module verifies that the library's published artifacts are consumable
by the minimum supported Gradle version. It requires the snapshot artifacts to be published to
Maven Local first. This is a separate step because running both in the same Gradle invocation causes
a race condition with Kotlin/Native:

```bash
./gradlew publishToMavenLocal -PsnapshotPublish=true   # publishes artifacts, including snapshot versions, to ~/.m2
./gradlew build                                         # runs all tests including the integration test
```

No GPG signing key is required for local development — pass `-Psigning.skip=true` to skip signing,
or add `signing.skip=true` to your local `gradle.properties`.

## Development guidelines

The following guidelines should be followed during development:

1. Public interfaces only belong in `api`, `api-ext`, or `sdk-api`.
2. `api` aims to remain as close to the [OTel specification](https://opentelemetry.io/docs/specs/otel/) as possible.
3. Enhancements and syntactic sugar that are not part of the OTel specification should be placed in `api-ext`.
4. 1 class per source-file is preferred
5. Invalid/default values should be implemented as constants to reduce object instantiation
6. Every API should be defined as an interface (or enum/sealed class) rather than a concrete type
7. Default implementations of function signatures in interfaces is strongly discouraged, as this blends the API/implementation
8. Default values for parameters is permissible. Default values for lambda parameters should be non-complex
9. Annotate new APIs with `@ExperimentalApi` until they are considered stable
10. Platform-specific code that isn't specific to a module (e.g. getting the current time) should go in `platform-implementations` to promote reuse

## Adding a new target

Each Kotlin Multiplatform target adds an ongoing maintenance cost, so please [open an issue](https://github.com/open-telemetry/opentelemetry-kotlin/issues)
to discuss new targets before opening a PR. The issue should ideally explain who will use the target
and the level of ongoing maintenance you're able to give.

A new target should meet the following requirements:

1. The target should be supported on all modules or document/track what it doesn't support yet
2. Shared tests in `commonTest` should pass on the target
3. Shared tests should run in CI, or an issue should be created if the GitHub runner doesn't support the target
4. The [supported targets](README.md#supported-targets) table in the README should be updated
5. An example app should be added to [examples](examples)

New targets start at Tier 2. A target can be promoted to Tier 1 once it has had substantial
real-world testing and production usage.
