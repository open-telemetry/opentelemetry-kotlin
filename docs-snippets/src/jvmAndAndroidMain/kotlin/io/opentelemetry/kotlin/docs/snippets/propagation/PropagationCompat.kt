package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.propagation.createMutableMapTextMapSetter
import io.opentelemetry.kotlin.toOtelKotlinApi
import io.opentelemetry.api.OpenTelemetry as JavaOpenTelemetry

// region propagation-compat
fun injectWithJavaPropagators(javaSdk: JavaOpenTelemetry) {
    val otel = javaSdk.toOtelKotlinApi()

    // otel.propagator delegates to javaSdk.propagators.textMapPropagator.
    val headers = mutableMapOf<String, String>()
    otel.propagator.inject(currentContext(), headers, createMutableMapTextMapSetter())
}
// end-region
