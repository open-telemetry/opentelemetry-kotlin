package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.propagation.createCompositePropagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.createW3CTraceContextPropagator

// region propagation-configure-sdk
fun createSdkWithPropagators(): OpenTelemetry = createOpenTelemetry {
    propagator {
        createCompositePropagator(
            createW3CTraceContextPropagator(), // traceparent + tracestate headers
            createW3CBaggagePropagator(), // baggage header
        )
    }
}
// end-region
