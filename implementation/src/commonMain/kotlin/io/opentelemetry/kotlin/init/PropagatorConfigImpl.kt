package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.propagation.CompositeTextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.W3CBaggagePropagator

@OptIn(ExperimentalApi::class)
internal class PropagatorConfigImpl : PropagatorConfigDsl {

    private var configured: TextMapPropagator = NoopOpenTelemetry.propagator

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        configured = CompositeTextMapPropagator(propagators.toList())
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = W3CBaggagePropagator
        return configured
    }

    internal fun buildPropagator(): TextMapPropagator = configured
}
