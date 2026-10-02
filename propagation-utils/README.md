# propagation-utils

Internal helpers for validating and sanitising propagation and context data (hex encoding, trace/span IDs,
`tracestate`, and baggage) that are shared across module boundaries.

This module is not part of the public API. Modules should depend on it via `implementation` so that its
symbols are never exposed to consumers. It must not depend on any other module so that it can be used by
`api`.
