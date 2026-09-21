package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.NoopOpenTelemetry

internal class PropagatorsImpl : Propagators {

    override fun none(): TextMapPropagator = NoopOpenTelemetry.propagator
}
