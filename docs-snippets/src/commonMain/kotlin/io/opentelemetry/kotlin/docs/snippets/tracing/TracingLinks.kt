package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.Tracer

// region tracing-links
// Each message in the batch was sent from a different trace, so the span links to all of them.
fun processBatch(tracer: Tracer, senders: List<SpanContext>) {
    val span = tracer.startSpan("process-batch") {
        senders.forEach { sender ->
            addLink(sender) {
                setStringAttribute("link.reason", "batched-message")
            }
        }
    }
    span.end()
}
// end-region
