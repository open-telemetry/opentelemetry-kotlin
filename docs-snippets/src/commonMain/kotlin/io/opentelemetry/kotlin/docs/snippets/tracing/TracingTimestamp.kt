package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.Tracer

// region tracing-timestamp
// Records an operation that was timed before the SDK was ready, e.g. app startup.
fun recordStartup(tracer: Tracer, startedAtMs: Long, finishedAtMs: Long) {
    val span = tracer.startSpan("app-start", startTimestamp = startedAtMs * 1_000_000)
    span.end(finishedAtMs * 1_000_000) // nanoseconds since the Unix epoch
}
// end-region
