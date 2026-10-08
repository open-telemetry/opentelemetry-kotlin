# OpenTelemetry Kotlin Versioning

This document describes the versioning and compatibility expectations for the
OpenTelemetry Kotlin SDK. The project implements the
[OpenTelemetry specification](https://opentelemetry.io/docs/specs/otel/), and
these expectations apply to all artifacts published from this repository.

## Versioning scheme

The project uses [Semantic Versioning 2.0.0](https://semver.org/) version
numbers in the form `MAJOR.MINOR.PATCH`. Published artifacts are released from
the same repository version.

The project is currently pre-1.0. Before 1.0, a minor release can include
breaking changes. Maintainers nevertheless aim to avoid unnecessary breaking
changes, especially for API components identified as stable below. Patch
releases are reserved for focused fixes that do not intentionally change the
public API.

The changelog records user-visible changes and migration notes. Consumers
should review the migration notes when upgrading between minor versions.

## API stability

The public OpenTelemetry API is implemented primarily by the `api` module.
Its component-level stability is tracked in the
[API stability matrix](api/README.md#component-stability-matrix).

- Components marked **stable** are treated as the most compatibility-sensitive
  API surface while the project remains pre-1.0. They are not annotated with
  `@ExperimentalApi`.
- Components marked **development** may evolve as the implementation and the
  OpenTelemetry specification evolve.
- Symbols annotated with `@ExperimentalApi` can change or be removed without
  notice between releases. Consumers must explicitly opt in before using them.

The build enables Kotlin explicit API mode and binary compatibility validation
for modules that declare public API. API dumps are also checked for duplicate
public declarations across modules. These checks help maintainers identify API
changes; they do not replace reviewing source or behavioral compatibility.

## Platform and tooling compatibility

The SDK is a Kotlin Multiplatform library. Its supported targets currently
include JVM, Android, JavaScript, iOS, and tvOS.

| Requirement | Supported minimum | Notes |
| --- | --- | --- |
| Kotlin language version for JVM and Android consumers | 2.0 | Verified by the minimum-version integration fixture. |
| Kotlin version for KLIB consumers (Apple and JavaScript targets) | 2.4.0 | KLIB consumers must use at least the Kotlin version used to build the published artifacts. |
| JDK for building JVM and Android targets | 11 | The project configures a JDK 11 toolchain. |
| Android `minSdk` | 21 | Applies to the Android library targets. |
| Android `minCompileSdk` | 34 | Recorded in AAR metadata for Android consumers. |
| Android Gradle Plugin | 8.0.2 | Recorded in AAR metadata as the minimum supported AGP version. |
| Gradle | 8.0.2 | Exercised by the Gradle integration test fixture. |

The exact versions are maintained in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml). Raising a minimum
supported version is a compatibility decision and should be documented in the
changelog with migration guidance when it affects consumers.

## Managing compatibility changes

When changing a public API or its supported platform/tooling range:

1. Prefer additive changes where practical, particularly for stable API
   components.
2. Mark APIs experimental until their stability expectations are clear.
3. Update the API dump with `./gradlew apiDump` when a public API change is
   intentional.
4. Update the API stability matrix, this document, and the changelog when the
   change affects documented compatibility expectations.
5. Run the relevant verification tasks before submitting a change. The full
   project build runs tests and static analysis; Android test compilation can
   be checked with `./gradlew assembleAndroidTest` when Android tests cannot be
   run locally.

## Snapshot builds and releases

Local snapshot publication uses the repository version with a `-SNAPSHOT`
suffix. It is intended for development and integration testing, not as a
stability guarantee for production consumers.

Project releases follow the process in [RELEASING.md](RELEASING.md). The
project aims for an approximately monthly release cadence, subject to
maintainer discretion.
