package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-trace-correlation
fun checkout(otel: OpenTelemetry) {
    val tracer = otel.tracerProvider.getTracer("com.example.checkout")
    val logger = otel.loggerProvider.getLogger("com.example.checkout")
    val span = tracer.startSpan("checkout")
    val ctx = currentContext().storeSpan(span)

    // Logs emitted while the span is current get its trace and span IDs.
    ctx.runWithContext {
        logger.emit(body = "Payment accepted", severityNumber = SeverityNumber.INFO)
    }

    // Or pass the context explicitly, e.g. from a callback that runs on another thread.
    logger.emit(body = "Receipt sent", severityNumber = SeverityNumber.INFO, context = ctx)

    span.end()
}
// end-region
