# Propagation

This guide explains how OpenTelemetry sends context from one process to another, so that a single
trace can follow a request through your app, your backend and beyond.

It assumes you know what a context is and how to make one current. If not, read the
[context guide](context.md) first.

- [Part 1: The basics](#part-1-the-basics)
    - [Why propagation exists](#why-propagation-exists)
    - [Carriers, inject and extract](#carriers-inject-and-extract)
    - [A first round trip](#a-first-round-trip)
    - [What goes on the wire](#what-goes-on-the-wire)
- [Part 2: Configuring propagators](#part-2-configuring-propagators)
    - [The default is noop](#the-default-is-noop)
    - [Built-in propagators](#built-in-propagators)
    - [Getting the propagator](#getting-the-propagator)
- [Part 3: Instrumenting a boundary](#part-3-instrumenting-a-boundary)
    - [Writing a TextMapSetter](#writing-a-textmapsetter)
    - [Writing a TextMapGetter](#writing-a-textmapgetter)
    - [The client side](#the-client-side)
    - [The server side](#the-server-side)
    - [Sending baggage](#sending-baggage)
    - [Remote parents](#remote-parents)
- [Part 4: Advanced](#part-4-advanced)
    - [Writing a custom propagator](#writing-a-custom-propagator)
- [Part 5: Compat mode](#part-5-compat-mode)

## Part 1: The basics

### Why propagation exists

A context lives in memory within one process. When your app makes an HTTP request to a
backend, the backend can't see your context, so its spans start a new trace. When observed from
a dashboard this can result in two unrelated traces rather one trace showing the whole request.

Propagation fixes this. Before the request leaves, the sender writes the important parts of its
context into the request, usually as headers. The receiver uses this information to continue
the same trace.

### Carriers, inject and extract

Propagation has three moving parts:

- A **carrier** is whatever travels between the processes and can hold string key/value pairs.
  This is usually an HTTP header.
- **Inject** writes values from a context into a carrier.
- **Extract** reads values from a carrier and returns a new context that contains them.

OpenTelemetry defines a `TextMapPropagator` interface that performs both injection and extraction.
As it doesn't have knowledge of the carrier type, you supply `TextMapGetter` and `TextMapSetter`
adapters:

- A `TextMapSetter<C>` writes a key/value pair into a carrier of type `C`.
- A `TextMapGetter<C>` reads keys and values from a carrier of type `C`.

For `Map<String, String>` carriers, the API provides ready-made adapters via
`createMutableMapTextMapSetter()` and `createMapTextMapGetter()`.

### A first round trip

First, configure the SDK with propagators. [Part 2](#part-2-configuring-propagators) explains this
in detail.

```kotlin propagation-configure-sdk
fun createSdkWithPropagators(): OpenTelemetry = createOpenTelemetry {
    propagator {
        createCompositePropagator(
            createW3CTraceContextPropagator(), // traceparent + tracestate headers
            createW3CBaggagePropagator(), // baggage header
        )
    }
}
```

Now inject a span into a map, then extract it again, as the receiving side would:

```kotlin propagation-first-round-trip
fun firstRoundTrip(otel: OpenTelemetry) {
    val span = otel.tracerProvider.getTracer("example")
        .startSpan("call-backend", spanKind = SpanKind.CLIENT)

    // Sender: write the context into a carrier. Here the carrier is a map of headers.
    val headers = mutableMapOf<String, String>()
    otel.propagator.inject(
        context = currentContext().storeSpan(span),
        carrier = headers,
        setter = createMutableMapTextMapSetter(),
    )
    println(headers) // {traceparent=00-<trace-id>-<span-id>-03}

    // Receiver: read the context back out of the carrier.
    val received = otel.propagator.extract(
        context = rootContext(),
        carrier = headers,
        getter = createMapTextMapGetter(),
    )
    val remoteParent = received.extractSpan().spanContext
    println(remoteParent.traceId == span.spanContext.traceId) // true
    println(remoteParent.isRemote) // true

    span.end()
}
```

Note that the extracted span context is marked as **remote**: it describes a span that lives in
another process. Extraction doesn't create a span. It gives you a context whose span can be used
as a parent, which [Part 3](#the-server-side) shows how to do.

### What goes on the wire

With the W3C propagators, a request carries up to three headers:

```text
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-03
tracestate: vendor1=value1,vendor2=value2
baggage: user.tier=gold,region=eu-west-1
```

- **`traceparent`** holds the span's identity
- **`tracestate`** holds vendor-specific data from the span context's `TraceState`
- **`baggage`** holds the context's baggage entries

These formats are defined by the W3C [Trace Context](https://www.w3.org/TR/trace-context/) and
[Baggage](https://www.w3.org/TR/baggage/) standards, so services built with any OpenTelemetry
SDK can read them.

## Part 2: Configuring propagators

### The default is noop

> [!IMPORTANT]
> If you don't configure a propagator, the SDK uses a **noop** propagator. It injects nothing and
> extracts nothing, so traces silently stop at process boundaries.

You can configure the SDK to use a specific propagator within `propagator { }` when the SDK
is initialized.

You can also create propagators before SDK initialization. This can be useful
if you need to propagate a traceparent from instrumentation, but initialize your SDK later in your
application's lifecycle.

### Built-in propagators

| Function                            | Module          | Headers                                                |
|-------------------------------------|-----------------|--------------------------------------------------------|
| `createW3CTraceContextPropagator()` | `api`           | `traceparent`, `tracestate`                            |
| `createW3CBaggagePropagator()`      | `api`           | `baggage`                                              |
| `createB3Propagator(format)`        | `propagator-b3` | `b3`, or `X-B3-TraceId`, `X-B3-SpanId`, `X-B3-Sampled` |
| `createNoopPropagator()`            | `api`           | none                                                   |

```kotlin propagation-built-ins
fun builtInPropagators() {
    val traceContext = createW3CTraceContextPropagator()
    val baggage = createW3CBaggagePropagator()
    val noop = createNoopPropagator()

    // fields() lists the headers each propagator reads and writes.
    println(traceContext.fields()) // [traceparent, tracestate]
    println(baggage.fields()) // [baggage]
    println(noop.fields()) // []
}
```

The SDK has a single propagator, so to use more than one, combine them with
`createCompositePropagator()`, which calls each propagator in order.

### Getting the propagator

Instrumentation should generally use `OpenTelemetry.propagator` rather than building its own. That
way the app decides which formats are used in one place, and every library follows it.

## Part 3: Instrumenting a boundary

This section shows how to add propagation to your own client and server code, using these
fictional HTTP library types:

```kotlin propagation-example-requests
// Stand-in for your HTTP client's request type.
class OutgoingRequest(val url: String) {
    private val headers = linkedMapOf<String, String>()

    fun setHeader(name: String, value: String) {
        headers[name] = value
    }

    fun headers(): Map<String, String> = headers
}

// Stand-in for your HTTP server's request type. A header can appear more than once.
class IncomingRequest(private val headers: Map<String, List<String>>) {

    fun headerNames(): Set<String> = headers.keys

    // HTTP header names are case-insensitive.
    fun headerValues(name: String): List<String> = headers.entries
        .filter { it.key.equals(name, ignoreCase = true) }
        .flatMap { it.value }
}
```

### Writing a TextMapSetter

A setter has one method, which writes a key/value pair into the carrier:

```kotlin propagation-setter
// TextMapSetter is a fun interface, so a lambda works.
val outgoingRequestSetter: TextMapSetter<OutgoingRequest> = TextMapSetter { request, key, value ->
    request.setHeader(key, value)
}
```

### Writing a TextMapGetter

A getter has three methods:

- `keys()` returns every key in the carrier.
- `get()` returns the first value for a key, or null if there isn't one.
- `getAll()` returns every value for a key, in order, or an empty list. Some headers, such as
  `baggage` and `tracestate`, may appear more than once, and propagators use this to read all of
  them.

```kotlin propagation-getter
object IncomingRequestGetter : TextMapGetter<IncomingRequest> {

    override fun keys(carrier: IncomingRequest): Collection<String> = carrier.headerNames()

    override fun get(carrier: IncomingRequest, key: String): String? =
        carrier.headerValues(key).firstOrNull()

    override fun getAll(carrier: IncomingRequest, key: String): List<String> =
        carrier.headerValues(key)
}
```

For HTTP carriers, looking up keys must ignore case.

### The client side

When your code calls another service:

1. Start a span of kind `CLIENT` to represent the call.
2. Inject a context that contains that span into the outgoing request.
3. Send the request, then end the span when the response arrives.

```kotlin propagation-client
fun sendRequest(otel: OpenTelemetry, request: OutgoingRequest) {
    val tracer = otel.tracerProvider.getTracer("http-client")

    // 1. Start a CLIENT span for the outgoing call.
    val span = tracer.startSpan("GET", spanKind = SpanKind.CLIENT)
    val ctx = currentContext().storeSpan(span)

    ctx.runWithContext {
        // 2. Inject the context that holds the CLIENT span, so the server becomes its child.
        otel.propagator.inject(ctx, request, outgoingRequestSetter)

        // 3. Send the request with your HTTP client.
    }
    span.end()
}
```

### The server side

When your code receives a request:

1. Extract a context from the incoming request.
2. Start a span of kind `SERVER`, using the extracted context as its parent.
3. Make a context that contains the new span current while you handle the request.

```kotlin propagation-server
fun handleRequest(otel: OpenTelemetry, request: IncomingRequest) {
    val tracer = otel.tracerProvider.getTracer("http-server")

    // 1. Rebuild the caller's context from the request headers.
    val remoteCtx = otel.propagator.extract(currentContext(), request, IncomingRequestGetter)

    // 2. Start a SERVER span whose parent is the caller's span.
    val span =
        tracer.startSpan("GET /orders", parentContext = remoteCtx, spanKind = SpanKind.SERVER)

    // 3. Make the span, plus any baggage the caller sent, current while handling the request.
    remoteCtx.storeSpan(span).runWithContext {
        val tier = currentContext().extractBaggage().getValue("user.tier")
        println("handling request for a $tier user")
    }
    span.end()
}
```

### Sending baggage

Baggage set on the client arrives in the server's extracted context, as long as both sides use
the W3C Baggage propagator:

```kotlin propagation-baggage
fun sendWithBaggage(otel: OpenTelemetry, request: OutgoingRequest) {
    // Add to the baggage that is already current rather than replacing it.
    val baggage = currentContext().extractBaggage().set("user.tier", "gold")
    val ctx = currentContext().storeBaggage(baggage)

    otel.propagator.inject(ctx, request, outgoingRequestSetter)
    println(request.headers()["baggage"]) // user.tier=gold
}
```

### Remote parents

You can build a `SpanContext` yourself with `createSpanContext()` and store it with `storeSpanContext()`:

```kotlin propagation-remote-parent
// traceId and spanId are hex strings, e.g. read from a message in a custom format.
fun processMessage(tracer: Tracer, traceId: String, spanId: String) {
    val remoteParent = createSpanContext(traceId, spanId) {
        isRemote = true
        isSampled = true
    }
    // Invalid IDs give an invalid SpanContext, and the span below starts a new trace.
    println(remoteParent.isValid)

    val parentCtx = rootContext().storeSpanContext(remoteParent)
    tracer.startSpan("process-message", parentContext = parentCtx, spanKind = SpanKind.CONSUMER)
        .end()
}
```

`createSpanContext()` never throws. If an ID isn't valid hex of the right length, or is all zeros,
you get an invalid span context, and spans that use it as a parent start a new trace.

## Part 4: Advanced

### Writing a custom propagator

To propagate a value of your own, or to support a header format that isn't built in, implement
`TextMapPropagator`. This example carries a tenant ID in an `x-tenant-id` header:

```kotlin propagation-custom-propagator
// Carries a tenant ID between services in an "x-tenant-id" header.
object TenantPropagator : TextMapPropagator {
    private const val HEADER = "x-tenant-id"
    private val FIELDS = listOf(HEADER)

    val TENANT_ID: ContextKey<String> = createContextKey("tenant.id")

    override fun fields(): Collection<String> = FIELDS

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        val tenant = context.get(TENANT_ID) ?: return // nothing to send
        setter.set(carrier, HEADER, tenant)
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        val tenant = getter.get(carrier, HEADER)
        if (tenant.isNullOrBlank()) {
            return context // missing or invalid: return the input context unchanged
        }
        return context.set(TENANT_ID, tenant)
    }
}

fun createSdkWithTenantPropagator(): OpenTelemetry = createOpenTelemetry {
    propagator {
        // A composite also stops an exception thrown by one propagator from reaching your app.
        createCompositePropagator(
            createW3CTraceContextPropagator(),
            createW3CBaggagePropagator(),
            TenantPropagator,
        )
    }
}
```

Follow these rules when writing one:

- **Never throw.** Your propagator runs inside other people's requests. If the carrier holds
  something you can't parse, ignore it.
- **Return the input context when there's nothing to extract**, and only change the values your
  propagator owns.
- **Inject nothing when there's nothing to send.**
- **List every key you use in `fields()`.**
- **Keep it stateless.**

### Using fields ()

`fields()` returns the keys a propagator reads and writes. It's useful when you reuse a carrier,
for example when retrying a request, and need to clear values from the previous attempt:

```kotlin propagation-fields
fun retryRequest(otel: OpenTelemetry, headers: MutableMap<String, String>) {
    // Remove the previous attempt's propagation headers before reusing the carrier.
    otel.propagator.fields().forEach { headers.remove(it) }
    otel.propagator.inject(currentContext(), headers, createMutableMapTextMapSetter())
}
```

## Part 5: Compat mode

The `propagator { }` block works the same way in compat mode, and the built-in propagators can be
used with a compat SDK.

If you create the Kotlin API from an existing opentelemetry-java SDK with `toOtelKotlinApi()`,
`OpenTelemetry.propagator` delegates to that SDK's `ContextPropagators`. Configure propagators on
the Java SDK, and the Kotlin API follows:

```kotlin propagation-compat
fun injectWithJavaPropagators(javaSdk: JavaOpenTelemetry) {
    val otel = javaSdk.toOtelKotlinApi()

    // otel.propagator delegates to javaSdk.propagators.textMapPropagator.
    val headers = mutableMapOf<String, String>()
    otel.propagator.inject(currentContext(), headers, createMutableMapTextMapSetter())
}
```
