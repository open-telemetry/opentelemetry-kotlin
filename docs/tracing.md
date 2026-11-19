# Tracing

This guide explains how to send traces with opentelemetry-kotlin. It assumes you've created an SDK
as described in [Getting started](getting-started.md).

- [Part 1: Recording spans](#part-1-recording-spans)
    - [Getting a tracer](#getting-a-tracer)
    - [Attributes](#attributes)
    - [Events](#events)
    - [Status](#status)
    - [Exceptions](#exceptions)
    - [Parent and child spans](#parent-and-child-spans)
    - [Span kind](#span-kind)
    - [Links](#links)
    - [Timestamps](#timestamps)
    - [Checking whether a span is recording](#checking-whether-a-span-is-recording)
- [Part 2: Configuring tracing](#part-2-configuring-tracing)
    - [Processors and exporters](#processors-and-exporters)
    - [Sampling](#sampling)
    - [Span limits](#span-limits)
    - [Configuring tracers by scope](#configuring-tracers-by-scope)
    - [Writing a span processor](#writing-a-span-processor)

## Part 1: Recording spans

A span measures one operation, such as a network request or a screen load. A trace is a tree of
spans that together describe a larger piece of work.

### Getting a tracer

Spans are created by a `Tracer`, which you get from the SDK's `tracerProvider`:

```kotlin tracing-first-span
fun firstSpan(otel: OpenTelemetry, work: () -> Unit) {
    // Get a tracer once and reuse it. The name identifies the code that creates the spans.
    val tracer = otel.tracerProvider.getTracer("com.example.checkout", version = "1.4.0")

    val span = tracer.startSpan("checkout")
    try {
        work()
    } finally {
        span.end() // a span is only exported once it ends
    }
}
```

The tracer's name is
its [instrumentation scope](https://opentelemetry.io/docs/specs/otel/glossary/#instrumentation-scope).
Use the name of your library, module or package, not the name of a class or an operation. You can
also pass a `version`, `schemaUrl` and scope `attributes`.

The span name describes the operation and should be low-cardinality (i.e. `"GET /orders/{id}"` 
rather than `"GET /orders/42"`).

You must call `end()` on every span you start. A span that never ends is never exported _and_
leaks memory.

### Attributes

Attributes are key-value pairs that describe a span. Set them when the span starts, or later while
it's running:

```kotlin tracing-attributes
@OptIn(IncubatingApi::class)
fun loadCart(tracer: Tracer, cartId: String, fetchItems: () -> Long) {
    // Attributes known up front should be set at creation, so samplers can see them.
    val span = tracer.startSpan("load-cart") {
        setStringAttribute(AppAttributes.APP_SCREEN_NAME, "cart") // a semantic convention key
        setStringAttribute("cart.id", cartId) // a custom key
    }
    try {
        // Attributes can also be set while the span is running.
        span.setLongAttribute("cart.items", fetchItems())
    } finally {
        span.end()
    }
}
```

Prefer setting attributes at creation where possible, as samplers only see attributes that exist
when the span starts.

Use a key from the [semantic conventions](https://opentelemetry.io/docs/specs/semconv/) where one
fits, so backends know what the attribute means. The `semconv` module
(`io.opentelemetry.kotlin:semconv`) has constants for every key. Most are marked `@IncubatingApi`
because they can change between semantic convention versions, so opt in with
`@OptIn(IncubatingApi::class)`. Where no key fits, namespace your own, like `cart.id` above.

### Events

An event marks something that happened at a point in time during a span. It has a name, a
timestamp and optional attributes:

```kotlin tracing-events
fun onPaymentStep(span: Span, step: String) {
    span.addEvent("payment.step") {
        setStringAttribute("payment.step.name", step)
    }
}
```

If the event is meaningful on its own consider emitting a log instead.

### Status

Every span has a status, which is `StatusData.Unset` by default. Set `StatusData.Error` when the
operation failed:

```kotlin tracing-status
fun fetchOrder(tracer: Tracer, fetch: () -> Int) {
    val span = tracer.startSpan("fetch-order")
    try {
        val statusCode = fetch()
        if (statusCode >= 500) {
            span.setStatus(StatusData.Error("Server returned $statusCode"))
        }
    } finally {
        span.end()
    }
}
```

`StatusData.Ok` explicitly marks the operation as successful.

### Exceptions

`wrapOperation()` runs a block, records any exception it throws and ends the span. The block returns
the span's status:

```kotlin tracing-wrap-operation
fun chargeCard(tracer: Tracer, charge: () -> Unit) {
    // Ends the span when the block returns. If the block throws, the exception is recorded on
    // the span, the status is set to Error and the exception is rethrown.
    tracer.startSpan("charge-card").wrapOperation {
        charge()
        StatusData.Ok
    }
}
```

The exception is recorded as an `exception` event with `exception.type`, `exception.message` and
`exception.stacktrace` attributes, following the
[exception semantic conventions](https://opentelemetry.io/docs/specs/semconv/exceptions/exceptions-spans/).

### Parent and child spans

Unless a parent is explicitly supplied, a new span's parent is the span in the
[current context](context.md#part-2-the-current-context). If there's no span in the current context,
the new span starts a new trace:

```kotlin tracing-nesting
fun placeOrder(tracer: Tracer) {
    val order = tracer.startSpan("place-order")
    val ctx = currentContext().storeSpan(order)

    // Spans started while "place-order" is in the current context are its children.
    ctx.runWithContext {
        tracer.startSpan("reserve-stock").end()
    }

    // Or pass the parent explicitly, e.g. from a callback that runs on another thread.
    tracer.startSpan("send-receipt", parentContext = ctx).end()

    order.end()
}
```

Starting a span does NOT add it to the current context. For further details on how context works,
including threads and coroutines, see the [context guide](context.md). To continue a trace in
another process, see the [propagation guide](propagation.md).

### Span kind

`spanKind` says how the span relates to other services:

```kotlin tracing-span-kind
fun callPaymentService(tracer: Tracer) {
    // CLIENT: this span covers an outgoing request to another service.
    val span = tracer.startSpan("POST /payments", spanKind = SpanKind.CLIENT)
    span.end()
}
```

### Links

A link connects a span to another span that isn't its parent, e.g. in another trace:

```kotlin tracing-links
// Each message in the batch was sent from a different trace, so the span links to all of them.
fun processBatch(tracer: Tracer, senders: List<SpanContext>) {
    val span = tracer.startSpan("process-batch") {
        senders.forEach { sender ->
            addLink(sender) {
                setStringAttribute("link.reason", "batched-message")
            }
        }
    }
    span.end()
}
```

Add links when the span starts if possible so that samplers can see them.

### Timestamps

Spans record their start and end times in nanoseconds since the Unix epoch. Both default to the
current time. Set them explicitly only when the operation happened earlier, e.g. when you measured
an operation before the SDK was initialized:

```kotlin tracing-timestamp
// Records an operation that was timed before the SDK was ready, e.g. app startup.
fun recordStartup(tracer: Tracer, startedAtMs: Long, finishedAtMs: Long) {
    val span = tracer.startSpan("app-start", startTimestamp = startedAtMs * 1_000_000)
    span.end(finishedAtMs * 1_000_000) // nanoseconds since the Unix epoch
}
```

### Checking whether a span is recording

A span that is dropped by the [sampler](#sampling) or a disabled tracer is still returned, but it
returns false from `isRecording()`. This can be checked to skip expensive work:

```kotlin tracing-recording
fun syncCart(tracer: Tracer, cart: List<String>) {
    val span = tracer.startSpan("sync-cart")
    // Skip building an expensive attribute if the span was sampled out.
    if (span.isRecording()) {
        span.setStringAttribute("cart.skus", cart.joinToString())
    }
    span.end()
}
```

`Tracer.enabled()` returns false if the tracer is [disabled](#configuring-tracers-by-scope), so you
can avoid creating spans altogether. The answer can change, so call each time rather than storing
a result.

## Part 2: Configuring tracing

### Processors and exporters

Spans go from a tracer to a processor, then to an exporter. These are configured via
`tracerProvider { export { ... } }` when initializing the SDK.

The SDK includes two processors:

- `batchSpanProcessor()` queues ended spans and exports them in batches. Use it in production.
- `simpleSpanProcessor()` exports each span as soon as it ends. Use it for debugging.

And these exporters:

- `otlpHttpSpanExporter()` sends spans to an OTLP backend over HTTP.
- `stdoutSpanExporter()` prints spans to stdout.

To send spans to more than one place, combine processors with `compositeSpanProcessor()`:

```kotlin tracing-export
fun initTracing(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service"
    tracerProvider {
        export {
            compositeSpanProcessor(
                batchSpanProcessor(otlpHttpSpanExporter { endpoint = "http://localhost:4318" }),
                simpleSpanProcessor(stdoutSpanExporter()),
            )
        }
    }
}
```

### Sampling

A sampler decides whether a span is recorded and exported. Sampling reduces the volume of telemetry
you send, at the cost of losing some traces. By default the SDK samples every new trace, and child
spans follow their parent's decision.

Configure a different sampler with `sampler { ... }`:

```kotlin tracing-sampler
fun initTracingWithSampler(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        sampler {
            // Keep 10% of new traces. Spans with a parent follow the parent's decision.
            parentBased(root = probability(0.1))
        }
    }
}
```

The available samplers are:

- `alwaysOn()` samples every span.
- `alwaysOff()` samples no spans.
- `probability(ratio)` samples a fraction of traces, based on the trace ID.
- `parentBased(root)` follows the parent span's decision, and uses `root` for spans without a
  parent. Wrap other samplers in it so that traces aren't broken up across services.
- `alwaysRecord(root)` records every span so processors can see it, but only exports the spans
  `root` samples.
- `composite { ... }` builds a
  [composable sampler](https://opentelemetry.io/docs/specs/otel/trace/sdk/#compositesampler) that
  uses consistent probability sampling across services.

### Span limits

Span limits stop a single span from using too much memory or bandwidth:

```kotlin tracing-limits
fun initTracingWithLimits(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        spanLimits {
            attributeCountLimit = 32 // extra attributes are dropped
            attributeValueLengthLimit = 1024 // longer string values are truncated
            eventCountLimit = 64 // extra events are dropped
            linkCountLimit = 16 // extra links are dropped
        }
    }
}
```

The SDK applies [default limits](https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-limits)
to spans. You can also limit the attributes on each event and link with
`attributeCountPerEventLimit` and `attributeCountPerLinkLimit`.

### Configuring tracers by scope

A `TracerConfigurator` returns a `TracerConfig` for each tracer, based on its instrumentation
scope. One example use-case is to turn off noisy tracing in 3rd party code:

```kotlin tracing-configurator
private val disabledConfig = object : TracerConfig {
    override val enabled = false
}
private val defaultConfig = object : TracerConfig {
    override val enabled = true
}

fun initTracingWithConfigurator(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        tracerConfigurator { scope ->
            when {
                scope.name.startsWith("com.thirdparty.") -> disabledConfig
                else -> defaultConfig
            }
        }
    }
}
```

A disabled tracer returns non-recording spans, and its `enabled()` returns false.
`TracerConfigurator` should return shared `TracerConfig` objects rather than creating new ones, and
should always return fast.

### Writing a span processor

Implement `SpanProcessor` to change spans before they're exported, e.g. to add an attribute to
every span. Put it before any export operations in a composite processor:

```kotlin tracing-custom-processor
@OptIn(IncubatingApi::class)
class SessionIdSpanProcessor(private val sessionId: () -> String) : SpanProcessor {
    override fun onStart(span: ReadWriteSpan, parentContext: Context) {
        span.setStringAttribute(SessionAttributes.SESSION_ID, sessionId())
    }

    override fun onEnding(span: ReadWriteSpan) = Unit
    override fun onEnd(span: ReadableSpan) = Unit

    // Lets the SDK skip the callbacks that do nothing.
    override fun isOnEndingRequired() = false
    override fun isEndRequired() = false

    override suspend fun forceFlush() = OperationResultCode.Success
    override suspend fun shutdown() = OperationResultCode.Success
}

fun initTracingWithProcessor(sessionId: () -> String): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        export {
            compositeSpanProcessor(
                SessionIdSpanProcessor(sessionId), // runs first, so every span gets the attribute
                batchSpanProcessor(otlpHttpSpanExporter { endpoint = "http://localhost:4318" }),
            )
        }
    }
}
```

The SDK calls `onStart()` when a span starts, `onEnding()` just before it ends, while the span can
still be changed, and `onEnd()` after it has ended. These run on the thread that started or ended
the span, so keep any implementation fast and don't block.
