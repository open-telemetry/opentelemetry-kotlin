# compat

This module implements the opentelemetry-kotlin API on top of the `opentelemetry-java` SDK. The
entrypoint is `createCompatOpenTelemetry()`, which returns an `OpenTelemetrySdk` that delegates
traces and logs to the OTel Java SDK. `toOtelJava<Token>()` and `toOtelKotlin<Token>()` extension
functions convert between the Java and Kotlin APIs.

This module only ships JVM and Android artifacts, because it depends on `opentelemetry-java`.

See the [compat mode guide](../docs/compat.md) for installation, mixing with `implementation`,
context sharing, error handling and limitations.
