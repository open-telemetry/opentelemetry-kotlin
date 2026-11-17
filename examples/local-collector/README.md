# local-collector

A lightweight OTLP/HTTP receiver for viewing telemetry locally while developing. It runs on the JVM
and needs no Docker or external collector.

It listens on port `4318` and accepts protobuf-encoded traces (`POST /v1/traces`) and logs
(`POST /v1/logs`). Received data is kept in memory (the newest 5,000 spans and logs) and shown in a
desktop window. The left pane lists the items and the right pane shows the selected one's
attributes,
resource, scope, events and links. Metrics and OTLP/JSON are not supported.

## Running

Open the desktop UI:

```shell
./gradlew :examples:local-collector:run
```

Or print each span and log to stdout without opening a window:

```shell
./gradlew :examples:local-collector:runHeadless
```

Set the `LOCAL_COLLECTOR_PORT` environment variable to listen on a port other than `4318`.

## Sending data

Point an OTLP exporter at the collector's base URL:

| Where the app runs            | URL                     |
|-------------------------------|-------------------------|
| JVM, JS (Node), iOS simulator | `http://localhost:4318` |
| Android emulator              | `http://10.0.2.2:4318`  |
| Physical device               | `http://<IP>:4318`      |

Android apps need a network security config that allows cleartext traffic to that host.

The JVM console example app sends data here by default:

```shell
./gradlew :examples:example-app:runConsoleExampleApp
```
