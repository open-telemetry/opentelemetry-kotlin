# semconv

This module provides Kotlin representations of OTel semantic conventions. It generates them by downloading the
OTel semantic convention schemas from Github and using [OTel weaver](https://github.com/open-telemetry/weaver)
to generate Kotlin source files. This workflow can be invoked by running `./gradlew generateSemanticConventions`.
