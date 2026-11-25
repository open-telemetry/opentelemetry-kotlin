# Spec notes

This repo aims to follow the [OpenTelemetry specification](https://opentelemetry.io/docs/specs/otel/).
Where the specification leaves a choice open (for example a `MAY` or `SHOULD` requirement), this
document records which choice the project made and why.

## Global state is discouraged

The specification allows SDKs to offer global accessors, such as a global `OpenTelemetry` instance,
global `TracerProvider` or `LoggerProvider` instances, or global propagators. opentelemetry-kotlin
deliberately doesn't provide them.

Instead, an app creates a single instance with `createOpenTelemetry` (or `createCompatOpenTelemetry`)
and passes it to the code that needs it, e.g. as a constructor parameter. Instrumentation and
libraries should take an `OpenTelemetry` parameter and read its `tracerProvider`, `loggerProvider`
and `propagator` from that, rather than looking them up from somewhere global.

This approach:

- Makes testing easier. Each test can create its own instance, e.g. one that exports to memory,
  without state leaking between tests or needing to be reset.
- Keeps dependencies explicit, so it's clear which code produces telemetry.
- Leaves the app in control of how telemetry is configured, rather than whichever code happened
  to register a global first.

If you want a singleton, you can wrap the instance you created in your own code, e.g. an `object`
in your app that holds it.

The current context is the exception. The specification requires it to be tracked implicitly, so
the SDK manages it for you. See the [context guide](docs/context.md).

New public APIs should follow this approach and not add global state.
