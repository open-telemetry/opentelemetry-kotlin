package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.propagation.createMapTextMapGetter
import io.opentelemetry.kotlin.propagation.createMutableMapTextMapSetter
import io.opentelemetry.kotlin.tracing.SpanKind

// region propagation-first-round-trip
fun firstRoundTrip(otel: OpenTelemetry) {
    val span = otel.tracerProvider.getTracer("example")
        .startSpan("call-backend", spanKind = SpanKind.CLIENT)

    // Sender: write the context into a carrier. Here the carrier is a map of headers.
    val headers = mutableMapOf<String, String>()
    otel.propagator.inject(
        context = currentContext().storeSpan(span),
        carrier = headers,
        setter = createMutableMapTextMapSetter(),
    )
    println(headers) // {traceparent=00-<trace-id>-<span-id>-03}

    // Receiver: read the context back out of the carrier.
    val received = otel.propagator.extract(
        context = rootContext(),
        carrier = headers,
        getter = createMapTextMapGetter(),
    )
    val remoteParent = received.span.spanContext
    println(remoteParent.traceId == span.spanContext.traceId) // true
    println(remoteParent.isRemote) // true

    span.end()
}
// end-region
