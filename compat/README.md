# compat

This module implements a compatibility layer so that `api` can capture telemetry
using the `opentelemetry-java` library. The entrypoint is `createCompatOpenTelemetry()` which returns
an `OpenTelemetry` implementation that captures traces and logs under the hood by delegating to the
OTel Java SDK.

There are also several extension functions named `toOtelJava<Token>()` and `toOtelKotlin<Token>()` that
allow for conversion between the Java/Kotlin API for some symbols. Effectively, these just expose
the underlying Java object, or decorate it with a Kotlin API.

It is possible to mix & match between `compat` and `implementation`.
This can be useful if it's necessary to access certain exporters that are only defined in the Java SDK for
example. However, generally speaking library users would be encouraged to avoid mixing & matching in
the long term as this use-case has less focused testing compared to using each module individually.

`opentelemetry-api` and `opentelemetry-sdk` are exported transitively, so there is no need to add
opentelemetry-java as a separate dependency to use the `toOtelJava<Token>()` and
`toOtelKotlin<Token>()` conversions. Use `compat-bom` to align opentelemetry-java with the version
`compat` was built against.

## Context

The Kotlin and Java APIs must agree on a single current context, otherwise spans and logs lose their
parent or their trace/span IDs.

There is one `CurrentContextStorage` per process (see the `api` README). `createCompatOpenTelemetry()`
and `toOtelKotlinApi()` install a storage that keeps the current context in whichever `ContextStorage`
is configured on opentelemetry-java (thread-local by default). `currentContext()`, `Context.attach()`
and the Java `Context.current()`/`makeCurrent()` then all read and write the same current context, so
the Kotlin and Java Context APIs can be used interchangeably.

- Configuration supplied via `context { ... }` to `createCompatOpenTelemetry()` is ignored and
  reported as a warning to the `SdkErrorHandler`. To customize storage, configure opentelemetry-java's
  `ContextStorage` instead.
- If a `CurrentContextStorage` is already installed (e.g. by an earlier SDK, or by a call to
  `installContextStorage()`), the compat SDK uses it instead and reports a warning, because the Kotlin
  and Java current contexts may then not be shared.
- `shutdown()` uninstalls the storage, but only if that SDK installed it.

When using `implementation` together with `toOtelJavaApi()`, opt in to storing the current context in
opentelemetry-java's `ContextStorage` with `createOtelJavaCurrentContextStorage()` when creating the SDK:

```kotlin
val otel = createOpenTelemetry {
    context {
        storage = createOtelJavaCurrentContextStorage()
    }
}
val javaApi = otel.toOtelJavaApi()
```

This lets Java code use `Context.current()`, `Context.makeCurrent()`, `Span.current()`,
`Baggage.current()` and similar APIs with the same current context as the Kotlin API. It also
respects any `ContextStorage` wrappers that are installed (e.g. by the Java agent). If
`toOtelJavaApi()` is called without this option, a warning is reported to the SDK's error handler.

Without the option, do not read or set the opentelemetry-java current context directly through
the APIs above. They use a separate store that the Kotlin SDK never sees. Use
`currentContext()` and `Context.attach()` instead, or call `makeCurrent()` on a span obtained from
the Java API.

This module only ships a JVM artifact because it depends on the `opentelemetry-java` library.

## Error handling

A compat SDK reports its errors to the `SdkErrorHandler` configured on it. The first SDK created in
the process also receives errors reported by code that has no SDK instance, such as the `api` module.
Those errors are buffered until an SDK starts (at most 128, the earliest kept) and are then replayed
to its handler in order. That SDK stops receiving them on `shutdown()`.

## Log record processors

opentelemetry-java only lets a log record processor change attributes. In compat mode, writes to
any other `ReadWriteLogRecord` property (such as `body`, `severityNumber`, or `spanContext`) are
ignored. The first write to each property by each processor is reported as a warning to the
`SdkErrorHandler`.
