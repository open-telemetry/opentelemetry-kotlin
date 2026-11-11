# config-declarative

Opt-in support for [declarative configuration](https://opentelemetry.io/docs/specs/otel/configuration/) files.
Pass a reader from this artifact to `configFile` in the SDK DSL. The file is read from the given path,
or from `OTEL_CONFIG_FILE` when no path is supplied:

```kotlin
createOpenTelemetry {
    configFile(yamlConfigReader())              // reads OTEL_CONFIG_FILE
    // configFile(yamlConfigReader(), "otel.yaml") // or an explicit path
}
```

The SDK artifacts don't depend on this one. Without it, a path in `OTEL_CONFIG_FILE` is reported
through the SDK error handler and ignored.

It is a separate artifact so that consumers who don't use declarative config don't pull in a YAML
parser. Files are read on platforms with a file system (JVM, Android and native). On JS, reading a
file reports an error through the SDK error handler.
