package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.OpenTelemetry

// region tracing-first-span
fun firstSpan(otel: OpenTelemetry, work: () -> Unit) {
    // Get a tracer once and reuse it. The name identifies the code that creates the spans.
    val tracer = otel.tracerProvider.getTracer("com.example.checkout", version = "1.4.0")

    val span = tracer.startSpan("checkout")
    try {
        work()
    } finally {
        span.end() // a span is only exported once it ends
    }
}
// end-region
