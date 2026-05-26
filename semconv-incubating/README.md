# semconv-incubating

This module provides Kotlin representations of incubating OTel semantic conventions. It generates them by downloading the
OTel semantic convention schemas from Github and using [OTel weaver](https://github.com/open-telemetry/weaver)
to generate Kotlin source files into the `io.opentelemetry.kotlin.semconv.incubating` package. This workflow can be
invoked by running `./gradlew :semconv-incubating:generateSemanticConventions`.
