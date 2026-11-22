package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.propagation.createNoopPropagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.createW3CTraceContextPropagator

// region propagation-built-ins
fun builtInPropagators() {
    val traceContext = createW3CTraceContextPropagator()
    val baggage = createW3CBaggagePropagator()
    val noop = createNoopPropagator()

    // fields() lists the headers each propagator reads and writes.
    println(traceContext.fields()) // [traceparent, tracestate]
    println(baggage.fields()) // [baggage]
    println(noop.fields()) // []
}
// end-region
