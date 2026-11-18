package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.propagation.createMutableMapTextMapSetter

// region propagation-fields
fun retryRequest(otel: OpenTelemetry, headers: MutableMap<String, String>) {
    // Remove the previous attempt's propagation headers before reusing the carrier.
    otel.propagator.fields().forEach { headers.remove(it) }
    otel.propagator.inject(currentContext(), headers, createMutableMapTextMapSetter())
}
// end-region
