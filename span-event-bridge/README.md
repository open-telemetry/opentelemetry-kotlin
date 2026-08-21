# span-event-bridge

This module contains the [event to span event bridge](https://opentelemetry.io/docs/specs/otel/logs/sdk/#event-to-span-event-bridge)
log record processor. It copies event-shaped log records onto the current span as a span event, so
that backends which only consume traces still observe events emitted through the Logs API. The
bridged log record continues through the rest of the log pipeline unchanged.

```kotlin
loggerProvider {
    export {
        compositeLogRecordProcessor(
            batchLogRecordProcessor(otlpExporter),
            spanEventBridgeLogRecordProcessor(),
        )
    }
}
```
