# context-impl

Real implementations of the `Context` and `Baggage` value types declared in `api`.

These are deliberately kept out of `implementation` so that they can also back `noop`. Following
the spec and other SDK distributions, `Context` and `Baggage` are API-level concerns that stay
functional even when no SDK is installed — otherwise instrumentation that propagates context would
silently drop it when the host app switched telemetry off.
