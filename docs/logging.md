# Logging

This guide explains how to send logs with opentelemetry-kotlin. It assumes you've created an SDK
as described in [Getting started](getting-started.md).

- [Part 1: Emitting logs](#part-1-emitting-logs)
    - [Getting a logger](#getting-a-logger)
    - [Severity](#severity)
    - [Attributes](#attributes)
    - [Structured bodies](#structured-bodies)
    - [Exceptions](#exceptions)
    - [Events](#events)
    - [Timestamps](#timestamps)
    - [Linking logs to traces](#linking-logs-to-traces)
    - [Checking whether a log will be emitted](#checking-whether-a-log-will-be-emitted)
- [Part 2: Configuring logging](#part-2-configuring-logging)
    - [Processors and exporters](#processors-and-exporters)
    - [Log limits](#log-limits)
    - [Configuring loggers by scope](#configuring-loggers-by-scope)
    - [Writing a log record processor](#writing-a-log-record-processor)

## Part 1: Emitting logs

### Getting a logger

Logs are emitted by a `Logger`, which you get from the SDK's `loggerProvider`:

```kotlin logging-first-log
fun firstLog(otel: OpenTelemetry) {
    // Get a logger once and reuse it. The name identifies the code that emits the logs.
    val logger = otel.loggerProvider.getLogger("com.example.checkout", version = "1.4.0")

    logger.emit(body = "Payment accepted", severityNumber = SeverityNumber.INFO)
}
```

The logger's name is
its [instrumentation scope](https://opentelemetry.io/docs/specs/otel/glossary/#instrumentation-scope).
Use the name of your library, module or package, not the name of a class or an operation. You can
also pass a `version`, `schemaUrl` and scope `attributes`.

Every parameter of `emit()` is optional. The rest of this part explains each one.

### Severity

`severityNumber` says how important a log is. `SeverityNumber` has the levels defined by the
[log data model](https://opentelemetry.io/docs/specs/otel/logs/data-model/#field-severitynumber):
`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` and `FATAL`, each with three finer levels such as
`INFO2`.

`severityText` is the severity as your source names it, e.g. `"W"` from a logging framework.

### Attributes

Attributes are key-value pairs that describe a log. Set them in the trailing lambda:

```kotlin logging-attributes
@OptIn(IncubatingApi::class)
fun logPayment(logger: Logger, cartId: String, items: Long) {
    logger.emit(
        body = "Payment accepted",
        severityNumber = SeverityNumber.INFO,
        severityText = "INFO",
    ) {
        setStringAttribute(AppAttributes.APP_SCREEN_NAME, "checkout") // a semantic convention key
        setStringAttribute("cart.id", cartId) // a custom key
        setLongAttribute("cart.items", items)
    }
}
```

Use a key from the [semantic conventions](https://opentelemetry.io/docs/specs/semconv/) where one
fits, so backends know what the attribute means. The `semconv` module
(`io.opentelemetry.kotlin:semconv`) has constants for every key. Most are marked `@IncubatingApi`
because they can change between semantic convention versions, so opt in with
`@OptIn(IncubatingApi::class)`. Where no key fits, namespace your own, like `cart.id` above.

### Structured bodies

The body is usually a `String`, but it can be any value in the
[log data model](https://opentelemetry.io/docs/specs/otel/logs/data-model/#field-body). Maps,
lists, arrays, numbers, booleans and `ByteArray` are exported as structured values, and nesting is
allowed:

```kotlin logging-structured-body
fun logStructured(logger: Logger) {
    logger.emit(
        body = mapOf(
            "message" to "Payment accepted",
            "amount" to 42.5,
            "items" to listOf("sku-1", "sku-2"),
        ),
        severityNumber = SeverityNumber.INFO,
    )
}
```

You can also pass an `AnyValue` to control the exact type. `AnyValue.NullValue` represents an
explicit empty body that is passed to processors and exporters. Passing Kotlin `null` leaves the
body unset. Any other object is converted with `toString()`.

Generally, it's preferred to set attributes instead as these are easier to process on most backends.

### Exceptions

Pass an exception to record it on the log:

```kotlin logging-exception
fun chargeCard(logger: Logger, charge: () -> Unit) {
    try {
        charge()
    } catch (e: IllegalStateException) {
        // Adds exception.type, exception.message and exception.stacktrace attributes.
        logger.emit(body = "Payment failed", severityNumber = SeverityNumber.ERROR, exception = e)
    }
}
```

### Events

An event is a log record with an `eventName`. The name identifies the type of event on the backend
and provides information on its expected attributes and body:

```kotlin logging-event
@OptIn(IncubatingApi::class)
fun recordButtonTap(logger: Logger) {
    logger.emit(eventName = "app.widget.click") {
        setStringAttribute(AppAttributes.APP_WIDGET_ID, "checkout")
    }
}
```

Use an event name from the semantic conventions where one fits, like `app.widget.click` above.
Otherwise, namespace your own name so it doesn't clash with names from other code.

### Timestamps

A log has two timestamps, both in nanoseconds since the Unix epoch:

- `timestamp` is when the event happened.
- `observedTimestamp` is when the SDK saw the log.

Both properties default to the current time if not explicitly set. You should generally only set
these properties when the log describes something that happened earlier, e.g. when you forward
lines from a log file:

```kotlin logging-timestamp
// Forwards a line read from a log file, which records when it was originally written.
fun forwardLine(logger: Logger, line: String, writtenAtMs: Long) {
    logger.emit(
        body = line,
        timestamp = writtenAtMs * 1_000_000, // nanoseconds since the Unix epoch
        severityNumber = SeverityNumber.WARN,
        severityText = "W", // the severity as the source wrote it
    )
}
```

### Linking logs to traces

Unless context is explicitly supplied, a log is associated with the span in the
[current context](context.md#part-2-the-current-context), if any exists:

```kotlin logging-trace-correlation
fun checkout(otel: OpenTelemetry) {
    val tracer = otel.tracerProvider.getTracer("com.example.checkout")
    val logger = otel.loggerProvider.getLogger("com.example.checkout")
    val span = tracer.startSpan("checkout")
    val ctx = currentContext().storeSpan(span)

    // Logs emitted while the span is current get its trace and span IDs.
    ctx.runWithContext {
        logger.emit(body = "Payment accepted", severityNumber = SeverityNumber.INFO)
    }

    // Or pass the context explicitly, e.g. from a callback that runs on another thread.
    logger.emit(body = "Receipt sent", severityNumber = SeverityNumber.INFO, context = ctx)

    span.end()
}
```

Starting a span does NOT add it to the current context. For further details on how context works,
see the [context guide](context.md).

### Checking whether a log will be emitted

`enabled()` returns false if a log with the given severity, event name and context would be
dropped. Use it to skip expensive work:

```kotlin logging-enabled
fun logCart(logger: Logger, cart: List<String>) {
    // Skip building an expensive body if the log would be dropped anyway.
    if (logger.enabled(severityNumber = SeverityNumber.DEBUG)) {
        logger.emit(body = cart.joinToString(), severityNumber = SeverityNumber.DEBUG)
    }
}
```

The answer can change so call `enabled()` each time rather than storing the result.

## Part 2: Configuring logging

### Processors and exporters

Logs go from a logger to a processor, then to an exporter. These are configured via
`loggerProvider { export { ... } }` when initializing the SDK.

The SDK includes two processors:

- `batchLogRecordProcessor()` queues logs and exports them in batches. Use it in production.
- `simpleLogRecordProcessor()` exports each log as soon as it's emitted. Use it for debugging.

And these exporters:

- `otlpHttpLogRecordExporter()` sends logs to an OTLP backend over HTTP.
- `stdoutLogRecordExporter()` prints logs to stdout.

To send logs to more than one place, combine processors with `compositeLogRecordProcessor()`:

```kotlin logging-export
fun initLogging(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service"
    loggerProvider {
        export {
            compositeLogRecordProcessor(
                batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = "http://localhost:4318" }),
                simpleLogRecordProcessor(stdoutLogRecordExporter()),
            )
        }
    }
}
```

### Log limits

Log limits stop a single log from using too much memory or bandwidth:

```kotlin logging-limits
fun initLoggingWithLimits(): OpenTelemetrySdk = createOpenTelemetry {
    loggerProvider {
        logLimits {
            attributeCountLimit = 32 // extra attributes are dropped
            attributeValueLengthLimit = 1024 // longer string values are truncated
        }
    }
}
```

The SDK applies [default limits](https://opentelemetry.io/docs/specs/otel/common/#attribute-limits)
to log attributes.

### Configuring loggers by scope

A `LoggerConfigurator` returns a `LoggerConfig` for each logger, based on its instrumentation
scope. One example use-case is to turn off a noisy logger in 3rd party code:

```kotlin logging-configurator
private val quietConfig = object : LoggerConfig {
    override val minimumSeverity = SeverityNumber.WARN
}
private val defaultConfig = object : LoggerConfig {}

fun initLoggingWithConfigurator(): OpenTelemetrySdk = createOpenTelemetry {
    loggerProvider {
        loggerConfigurator { scope ->
            when {
                scope.name.startsWith("com.thirdparty.") -> quietConfig
                else -> defaultConfig
            }
        }
    }
}
```

`LoggerConfigurator` should return shared `LoggerConfig` objects rather than creating new ones, and
should always return fast.

### Writing a log record processor

Implement `LogRecordProcessor` to change logs before they're exported, e.g. to add an attribute to
every log. Put it before any export operations in a composite processor:

```kotlin logging-custom-processor
@OptIn(IncubatingApi::class)
class SessionIdProcessor(private val sessionId: () -> String) : LogRecordProcessor {
    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        log.setStringAttribute(SessionAttributes.SESSION_ID, sessionId())
    }

    override suspend fun forceFlush() = OperationResultCode.Success
    override suspend fun shutdown() = OperationResultCode.Success
}

fun initLoggingWithProcessor(sessionId: () -> String): OpenTelemetrySdk = createOpenTelemetry {
    loggerProvider {
        export {
            compositeLogRecordProcessor(
                SessionIdProcessor(sessionId), // runs first, so the exported log has the attribute
                batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = "http://localhost:4318" }),
            )
        }
    }
}
```

`onEmit()` runs on the thread that emitted the log, so keep any implementation fast and don't block.
