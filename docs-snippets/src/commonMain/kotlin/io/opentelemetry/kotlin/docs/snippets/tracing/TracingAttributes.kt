package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.Tracer

// region tracing-attributes
fun loadCart(tracer: Tracer, cartId: String, fetchItems: () -> Long) {
    // Attributes known up front should be set at creation, so samplers can see them.
    val span = tracer.startSpan("load-cart") {
        setStringAttribute("cart.id", cartId)
    }
    try {
        // Attributes can also be set while the span is running.
        span.setLongAttribute("cart.items", fetchItems())
    } finally {
        span.end()
    }
}
// end-region
