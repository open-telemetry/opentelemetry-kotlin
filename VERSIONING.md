# OpenTelemetry Kotlin Versioning

This document explains how opentelemetry-kotlin meets the
[OpenTelemetry versioning and stability spec](https://opentelemetry.io/docs/specs/otel/versioning-and-stability/).

## Version numbers

This project follows [Semantic Versioning 2.0.0](https://semver.org/). Versions have the form
`MAJOR.MINOR.PATCH`.

- All artifacts are released together and share the same version.
- Use the `bom` to keep artifact versions aligned. If you use `compat`, use `compat-bom` instead.
- Most releases bump `MINOR`. `PATCH` releases are only for urgent fixes.

## Before 1.0

The project is not yet stable. While the version is `0.x`:

- Any `MINOR` release may contain breaking changes.
- Breaking changes are listed in [CHANGELOG.md](CHANGELOG.md).

The rules below apply from `1.0.0` onwards.

## Stability guarantees

A stable API will not have breaking changes unless the `MAJOR` version is bumped.

A breaking change is any change that:

- stops Kotlin code that uses the API from compiling (source compatibility)
- stops already compiled JVM or Android code from running without a rebuild (binary compatibility)
- changes behavior in a way that forces callers to change their code

The SDK configuration DSL is covered by these guarantees.

These are not covered:

- Binary compatibility of klibs (iOS, tvOS, JS). Source compatibility still applies.
- Calling the API from Java.
- Code marked with an opt-in annotation (see below).
- Code in a package that contains `internal`, even if it is public.
- Modules that are not published as a public API.

API changes are checked on every PR with ABI dump files (see [CONTRIBUTING.md](CONTRIBUTING.md)).

## Unstable code

Unstable code is marked with an opt-in annotation:

- `@ExperimentalApi` marks APIs that are not yet stable.
- `@IncubatingApi` marks semantic conventions that are still incubating.

Code with these annotations may change or be removed in any release. When an API becomes stable,
its annotation is removed in a `MINOR` release.

## Deprecation

Stable APIs are deprecated before they are removed. A deprecated API stays until the next `MAJOR`
release.

## Support for old major versions

When a new `MAJOR` version of the API is released, the previous `MAJOR` version is supported for at
least 3 years. This means:

- no breaking changes
- an SDK that supports its latest `MINOR` version is maintained
- bug and security fixes are backported
- new features are usually not backported

When a new `MAJOR` version of the SDK is released, the previous `MAJOR` version is supported for at
least 1 year.

## Platform support

The minimum supported versions are:

| Tool                    | Minimum version |
|-------------------------|-----------------|
| Kotlin language/API     | 2.0             |
| Kotlin Gradle plugin    | 2.0.0 (JVM and Android), 2.4.0 (iOS, tvOS, JS) |
| Gradle                  | 8.0.2           |
| JVM                     | 11              |
| Android                 | API 21          |

The `gradle-integration-test` module checks these minimums on every PR.

Minimum versions may be raised in a `MINOR` release. Any change is noted in
[CHANGELOG.md](CHANGELOG.md).

## The `compat` module

`compat` is built on top of [opentelemetry-java](https://github.com/open-telemetry/opentelemetry-java).
A `MINOR` release may raise the opentelemetry-java version it depends on. Use `compat-bom` to keep
these versions aligned.
