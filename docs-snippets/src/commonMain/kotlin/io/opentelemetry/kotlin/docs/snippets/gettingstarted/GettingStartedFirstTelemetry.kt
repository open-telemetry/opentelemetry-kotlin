package io.opentelemetry.kotlin.docs.snippets.gettingstarted

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.logging.SeverityNumber

// region getting-started-first-telemetry
fun checkout(otel: OpenTelemetry) {
    val tracer = otel.tracerProvider.getTracer("com.example.checkout")
    val logger = otel.loggerProvider.getLogger("com.example.checkout")

    val span = tracer.startSpan("checkout")
    try {
        span.setStringAttribute("cart.id", "cart-42")
        logger.emit(body = "Payment accepted", severityNumber = SeverityNumber.INFO)
    } finally {
        span.end() // a span is only exported once it ends
    }
}
// end-region
