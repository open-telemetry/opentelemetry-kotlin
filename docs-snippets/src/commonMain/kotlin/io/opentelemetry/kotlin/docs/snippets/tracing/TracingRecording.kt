package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.Tracer

// region tracing-recording
fun syncCart(tracer: Tracer, cart: List<String>) {
    val span = tracer.startSpan("sync-cart")
    // Skip building an expensive attribute if the span was sampled out.
    if (span.isRecording()) {
        span.setStringAttribute("cart.skus", cart.joinToString())
    }
    span.end()
}
// end-region
