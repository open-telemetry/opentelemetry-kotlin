package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext
import io.opentelemetry.kotlin.tracing.SpanKind

// region propagation-server
fun handleRequest(otel: OpenTelemetry, request: IncomingRequest) {
    val tracer = otel.tracerProvider.getTracer("http-server")

    // 1. Rebuild the caller's context from the request headers.
    val remoteCtx = otel.propagator.extract(currentContext(), request, IncomingRequestGetter)

    // 2. Start a SERVER span whose parent is the caller's span.
    val span =
        tracer.startSpan("GET /orders", parentContext = remoteCtx, spanKind = SpanKind.SERVER)

    // 3. Make the span, plus any baggage the caller sent, current while handling the request.
    remoteCtx.storeSpan(span).runWithContext {
        val tier = currentContext().extractBaggage().getValue("user.tier")
        println("handling request for a $tier user")
    }
    span.end()
}
// end-region
