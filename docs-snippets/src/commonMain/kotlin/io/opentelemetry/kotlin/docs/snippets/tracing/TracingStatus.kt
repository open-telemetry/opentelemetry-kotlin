package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.Tracer

// region tracing-status
fun fetchOrder(tracer: Tracer, fetch: () -> Int) {
    val span = tracer.startSpan("fetch-order")
    try {
        val statusCode = fetch()
        if (statusCode >= 500) {
            span.setStatus(StatusData.Error("Server returned $statusCode"))
        }
    } finally {
        span.end()
    }
}
// end-region
