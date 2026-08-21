# config-schema

This module provides Kotlin representations of the OTel declarative configuration schema. It
generates them by downloading the
[opentelemetry-configuration](https://github.com/open-telemetry/opentelemetry-configuration)
JSON schema from Github and generating Kotlin source files from it. This workflow can be invoked
by running `./gradlew generateOpenTelemetryConfiguration`.

These types mirror the schema exactly. `behavior` is the behavior the SDK is configured with.
