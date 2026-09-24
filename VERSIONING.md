# OpenTelemetry Kotlin versioning and stability

This document describes how this repository applies the OpenTelemetry
[versioning and stability requirements](https://opentelemetry.io/docs/specs/otel/versioning-and-stability/)
to its Kotlin Multiplatform artifacts.

## Version numbers

OpenTelemetry Kotlin follows [Semantic Versioning 2.0.0](https://semver.org/).
All published artifacts from this repository use the same release version. Consumers should keep
their OpenTelemetry Kotlin dependencies aligned, preferably with the published `compat-bom`.

The project is currently pre-1.0. During this development period:

- breaking changes to development APIs require a minor version bump and migration notes;
- backward-compatible features require a minor version bump;
- backward-compatible bug fixes, security fixes, and documentation-only changes require a patch
  version bump.

After 1.0, breaking changes to stable public APIs require a major version bump. Adding
backward-compatible functionality requires a minor version bump, and fixes that do not require
consumer recompilation require a patch version bump.

## API maturity

New public APIs are development APIs and are annotated with `@ExperimentalApi` until their design
has been reviewed and declared stable. Experimental APIs may change or be removed in a minor
release.

A public API becomes stable only when the project explicitly announces that transition in the
changelog and removes its experimental marker. Once stable, its public declarations remain source
compatible across minor and patch releases of the same major version. Where Kotlin Multiplatform
tooling supports it, stable published artifacts also preserve binary compatibility for supported
targets.

Internal declarations, generated implementation details, and APIs carrying an explicit experimental
or opt-in marker are not covered by the stable API guarantee.

## SDK and artifact compatibility

Public SDK extension points and constructors follow the same maturity rules as API declarations.
Stable SDK surface remains backward compatible within a major version. Internal SDK components may
change in a minor release.

Artifacts in this repository may use internal APIs from sibling artifacts. Mixing different release
versions can therefore cause compilation or runtime failures and is unsupported.

## Deprecation and removal

A stable API is deprecated only when a stable replacement is available. Deprecations include
migration guidance and continue to receive the same compatibility guarantees as other stable APIs.
Removing a stable API requires a major version bump.

Development APIs may be removed in a minor release, with the change documented in the changelog.

## Kotlin and platform support

The supported Kotlin, Gradle, Android Gradle Plugin, Android SDK, and Apple toolchain versions are
documented by the build and contributing guides. Raising a minimum supported toolchain or platform
version is a deliberate change and is documented in the changelog with migration guidance.

Before 1.0, such a change requires at least a minor release. After 1.0, its release level follows the
conventions of the Kotlin Multiplatform ecosystem and the compatibility impact on consumers.

## Semantic conventions and telemetry

The `semconv` artifact is generated from a specific released OpenTelemetry semantic conventions
version. The selected version and schema URL are recorded in the build. Changes to emitted telemetry
follow the OpenTelemetry
[telemetry stability requirements](https://opentelemetry.io/docs/specs/otel/telemetry-stability/).

## Release support

Releases are made from a single versioned source tree. Bug and security fixes are normally applied
to the latest minor release; older minor releases do not receive routine backports.

If a future major version is released, support for the preceding stable API major version follows
the OpenTelemetry long-term-support requirements.
