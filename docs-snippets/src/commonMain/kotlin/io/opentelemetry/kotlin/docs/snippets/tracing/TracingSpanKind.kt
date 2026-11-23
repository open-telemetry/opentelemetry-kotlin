package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.Tracer

// region tracing-span-kind
fun callPaymentService(tracer: Tracer) {
    // CLIENT: this span covers an outgoing request to another service.
    val span = tracer.startSpan("POST /payments", spanKind = SpanKind.CLIENT)
    span.end()
}
// end-region
