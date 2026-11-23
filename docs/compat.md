# Compat mode

Compat mode implements the opentelemetry-kotlin API on top of the
[opentelemetry-java](https://github.com/open-telemetry/opentelemetry-java) SDK. Your code records
telemetry with the Kotlin API, and opentelemetry-java processes and exports it. This means that
you can reuse opentelemetry-java exporters, instrumentation and configuration from Kotlin.

Compat mode is only available on the JVM and Android, because it depends on opentelemetry-java.

- [Installing](#installing)
- [Creating a compat SDK](#creating-a-compat-sdk)
- [Converting between the Java and Kotlin APIs](#converting-between-the-java-and-kotlin-apis)
- [Mixing compat and implementation](#mixing-compat-and-implementation)
- [Context](#context)
- [Error handling](#error-handling)
- [Log record processors](#log-record-processors)

## Installing

Depend on `compat` and import `compat-bom`. The BOM aligns the opentelemetry-kotlin artifacts, and
also the opentelemetry-java artifacts with the version that `compat` was built against:

```kotlin getting-started-install-compat
dependencies {
    // compat-bom also aligns the opentelemetry-java artifacts that compat uses
    implementation(platform("io.opentelemetry.kotlin:compat-bom:0.10.0"))
    implementation("io.opentelemetry.kotlin:compat")
    implementation("io.opentelemetry.kotlin:exporters-otlp")
}
```

## Creating a compat SDK

`createCompatOpenTelemetry()` takes the same configuration DSL as `createOpenTelemetry()`, and
returns an SDK that delegates to opentelemetry-java:

```kotlin compat-create
fun initCompatTelemetry(): OpenTelemetrySdk = createCompatOpenTelemetry {
    serviceName = "checkout-service"
    tracerProvider {
        export { batchSpanProcessor(otlpHttpSpanExporter { endpoint = "http://localhost:4318" }) }
    }
}
```

If your app already configures an opentelemetry-java SDK, wrap it instead of creating a new one:

```kotlin compat-wrap-java
// Wrap an opentelemetry-java SDK that your app already configures, e.g. with autoconfigure.
fun wrapJavaSdk(javaOtel: JavaOpenTelemetry): OpenTelemetrySdk = javaOtel.toOtelKotlinApi()
```

## Converting between the Java and Kotlin APIs

Extension functions named `toOtelJava<Token>()` and `toOtelKotlin<Token>()` convert between the
Java and Kotlin API for some types. They either expose the underlying Java object or decorate it
with a Kotlin API. For example, `toOtelJavaApi()` returns the opentelemetry-java `OpenTelemetry`
that a compat SDK delegates to, which you can pass to Java instrumentation.

## Mixing compat and implementation

You can mix `compat` and `implementation`, e.g. to use an exporter that only exists in the Java
SDK. However, avoid doing this in the long term. Mixing the two has less focused testing than using
either module on its own.

When you call `toOtelJavaApi()` on an `implementation` SDK, store the current context in
opentelemetry-java's `ContextStorage` by installing `createOtelJavaCurrentContextStorage()` when
you create the SDK:

```kotlin compat-mix-with-implementation
fun javaApiForImplementation(): JavaOpenTelemetry {
    val otel = createOpenTelemetry {
        // Keep the current context in opentelemetry-java's ContextStorage.
        context { storage { createOtelJavaCurrentContextStorage() } }
    }
    return otel.toOtelJavaApi()
}
```

Java code can then use `Context.current()`, `Context.makeCurrent()`, `Span.current()`,
`Baggage.current()` and similar APIs with the same current context as the Kotlin API. This also
respects any `ContextStorage` wrappers that are installed, e.g. by the Java agent. If you call
`toOtelJavaApi()` without this storage, a warning is reported to the SDK's error handler.

Without the storage, don't read or set the opentelemetry-java current context through the APIs
above. They use a separate store that the Kotlin SDK never sees. Use `currentContext()` and
`Context.attach()` instead, or call `makeCurrent()` on a span obtained from the Java API.

## Context

The Kotlin and Java APIs must agree on a single current context. Otherwise, spans and logs lose
their parent or their trace and span IDs.

There's one `CurrentContextStorage` per process (see the [context guide](context.md)).
`createCompatOpenTelemetry()` and `toOtelKotlinApi()` install a storage that keeps the current
context in whichever `ContextStorage` is configured on opentelemetry-java, which is thread-local by
default. `currentContext()`, `Context.attach()` and the Java `Context.current()`/`makeCurrent()`
then all read and write the same current context, so you can use the Kotlin and Java Context APIs
interchangeably.

- Configuration supplied to `createCompatOpenTelemetry()` with `context { ... }` is ignored and
  reported as a warning to the `SdkErrorHandler`. To customize storage, configure
  opentelemetry-java's `ContextStorage` instead.
- If a `CurrentContextStorage` is already installed, e.g. by an earlier SDK or a call to
  `installContextStorage()`, the compat SDK uses it instead and reports a warning, because the
  Kotlin and Java current contexts may then not be shared.
- `shutdown()` uninstalls the storage, but only if that SDK installed it.

To convert a single context between the two APIs, see
[Part 4 of the context guide](context.md#part-4-compat-mode).

## Error handling

A compat SDK reports its errors to the `SdkErrorHandler` configured on it. The first SDK created in
the process also receives errors reported by code that has no SDK instance, such as the `api`
module. Those errors are buffered until an SDK starts (at most 128, keeping the earliest) and then
replayed to its handler in order. That SDK stops receiving them on `shutdown()`.

## Log record processors

opentelemetry-java only lets a log record processor change attributes. In compat mode, writes to
any other `ReadWriteLogRecord` property, such as `body`, `severityNumber` or `spanContext`, are
ignored. The first write to each property by each processor is reported as a warning to the
`SdkErrorHandler`.
