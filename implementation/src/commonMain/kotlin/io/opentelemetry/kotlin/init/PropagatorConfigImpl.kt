package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.B3Propagator
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.W3CTraceContextPropagator
import io.opentelemetry.kotlin.propagation.createCompositePropagator
import io.opentelemetry.kotlin.propagation.createNoopPropagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator

@OptIn(ExperimentalApi::class)
internal class PropagatorConfigImpl : PropagatorConfigDsl {

    private var configured: TextMapPropagator = createNoopPropagator()

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        configured = createCompositePropagator(*propagators)
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = createW3CBaggagePropagator()
        return configured
    }

    override fun w3cTraceContext(): TextMapPropagator {
        configured = W3CTraceContextPropagator()
        return configured
    }

    override fun b3(format: B3Format): TextMapPropagator {
        configured = B3Propagator(format)
        return configured
    }

    internal fun buildPropagator(): TextMapPropagator = configured
}
