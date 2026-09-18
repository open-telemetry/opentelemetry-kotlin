# api-propagators

Constructs `TextMapPropagator` instances without requiring an `OpenTelemetry` instance.

Instrumentation authors that read and write cross-cutting context may not want to wait for the SDK
to initialize, which is why Propagators in OpenTelemetry _must_ be accessible separately from the SDK
instance.

`createPropagators()` builds propagators standalone, with no global state. When invoking `createOpenTelemetry()`
is is possible to pass the propagators API and the SDK will reuse rather than building its own.
