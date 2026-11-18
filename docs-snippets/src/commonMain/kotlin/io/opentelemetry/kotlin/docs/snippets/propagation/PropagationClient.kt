package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext
import io.opentelemetry.kotlin.tracing.SpanKind

// region propagation-client
fun sendRequest(otel: OpenTelemetry, request: OutgoingRequest) {
    val tracer = otel.tracerProvider.getTracer("http-client")

    // 1. Start a CLIENT span for the outgoing call.
    val span = tracer.startSpan("GET", spanKind = SpanKind.CLIENT)
    val ctx = currentContext().storeSpan(span)

    ctx.runWithContext {
        // 2. Inject the context that holds the CLIENT span, so the server becomes its child.
        otel.propagator.inject(ctx, request, outgoingRequestSetter)

        // 3. Send the request with your HTTP client.
    }
    span.end()
}
// end-region
