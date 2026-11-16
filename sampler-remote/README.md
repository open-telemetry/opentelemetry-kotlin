# Remote sampling strategies

Add `io.opentelemetry.kotlin:sampler-remote` alongside the Kotlin SDK to use
Jaeger sampling strategy JSON. In the SDK's sampler configuration, call
`remoteSamplerFromJson(responseBody, errorHandler) ?: alwaysOn()`, or choose
another fallback suited to your application.

The helper supports probabilistic and per-operation strategies. It returns
`null` if it cannot build a sampler. The application fetches the JSON and
decides how to apply refreshed strategies.
