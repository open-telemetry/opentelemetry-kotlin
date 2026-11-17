package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.kotlin.tracing.createSpanContext

// region propagation-remote-parent
// traceId and spanId are hex strings, e.g. read from a message in a custom format.
fun processMessage(tracer: Tracer, traceId: String, spanId: String) {
    val remoteParent = createSpanContext(traceId, spanId) {
        isRemote = true
        isSampled = true
    }
    // Invalid IDs give an invalid SpanContext, and the span below starts a new trace.
    println(remoteParent.isValid)

    val parentCtx = rootContext().storeSpanContext(remoteParent)
    tracer.startSpan("process-message", parentContext = parentCtx, spanKind = SpanKind.CONSUMER)
        .end()
}
// end-region
