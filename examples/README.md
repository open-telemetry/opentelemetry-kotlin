# examples

Example apps that demonstrate how to use opentelemetry-kotlin on different platforms.

| Directory                                              | What it is                                                                             |
|--------------------------------------------------------|----------------------------------------------------------------------------------------|
| [`example-app`](example-app/README.md)                 | Kotlin Multiplatform app (desktop, console, Node.js and iOS) that emits spans and logs |
| [`example-app-android`](example-app-android/README.md) | Android launcher for `example-app`'s shared UI                                         |
| [`local-collector`](local-collector/README.md)         | Desktop OTLP/HTTP receiver for viewing the telemetry the examples send                 |

## Seeing the data

Every example app exports spans and logs over OTLP/HTTP to `local-collector` on port `4318`, with
`service.name` set to `otel-kotlin-example`. Start the collector first, then run an app in another
terminal:

```sh
./gradlew :examples:local-collector:run
./gradlew :examples:example-app:runConsoleExampleApp
```

The [example-app README](example-app/README.md) lists the run command for each platform.
