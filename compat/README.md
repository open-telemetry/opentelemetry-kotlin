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

## Context

The Kotlin and Java APIs must agree on a single implicit context, otherwise spans and logs lose their
parent or their trace/span IDs.

`createCompatOpenTelemetry()` and `toOtelKotlinApi()` always store the implicit context in whichever
`ContextStorage` is configured on opentelemetry-java (thread-local by default), so the Kotlin and Java
Context APIs can be used interchangeably. Configuration supplied via `context { ... }` to
`createCompatOpenTelemetry()` is ignored and reported as a warning to the `SdkErrorHandler`. To customize
storage, configure opentelemetry-java's `ContextStorage` instead.

When using `implementation` together with `toOtelJavaApi()`, set
`storageMode = ImplicitContextStorageMode.THREAD_LOCAL` for compatibility with the Java SDK's default
behavior. In that setup you should not read or set the opentelemetry-java implicit context directly, as
it is a separate store that the Kotlin SDK never sees. This includes:

- `Context.current()` / `OtelJavaContext.current()`
- `Context.makeCurrent()` (e.g. `Context.current().with(span).makeCurrent()`)
- `Span.current()` and `Baggage.current()`
- `Baggage.makeCurrent()`

Use `OpenTelemetry.context.implicit()` and `Context.attach()` instead, or call `makeCurrent()` on a span
obtained from the Java API.

This module only ships a JVM artifact because it depends on the `opentelemetry-java` library.
