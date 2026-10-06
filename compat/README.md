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
parent or their trace/span IDs. For compatibility with the Java SDK's default behavior, set
`storageMode = ImplicitContextStorageMode.THREAD_LOCAL`.

When using `toOtelJavaApi()` you should not read or set the opentelemetry-java implicit context
directly, as it is a separate store that the Kotlin SDK never sees. This includes:

- `Context.current()` / `OtelJavaContext.current()`
- `Context.makeCurrent()` (e.g. `Context.current().with(span).makeCurrent()`)
- `Span.current()` and `Baggage.current()`
- `Baggage.makeCurrent()`

Use `OpenTelemetry.context.implicit()` and `Context.attach()` instead, or call `makeCurrent()` on a span
obtained from the Java API.

This module only ships a JVM artifact because it depends on the `opentelemetry-java` library.
