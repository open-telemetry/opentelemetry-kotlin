# propagator-b3

`createB3Propagator()`: a `TextMapPropagator` that injects and extracts the current span context
using [B3 headers](https://github.com/openzipkin/b3-propagation). Extraction accepts both the single
`b3` header and the multi `X-B3-*` headers. Injection uses the `B3Format` you pass in.

The OpenTelemetry specification requires B3 to ship separately from the API
([Propagators Distribution](https://opentelemetry.io/docs/specs/otel/context/api-propagators/#propagators-distribution)),
so it lives in its own module. To use it with the SDK, pass it to `propagator { }`:

```kotlin
propagator {
    composite(w3cTraceContext(), createB3Propagator(B3Format.MULTI))
}
```
