package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.init.PropagatorConfigDsl
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.createB3Propagator
import io.opentelemetry.kotlin.propagation.createCompositePropagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.createW3CTraceContextPropagator

@ExperimentalApi
class PropagatorConfigImpl : PropagatorConfigDsl {

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator =
        createCompositePropagator(*propagators)

    override fun w3cBaggage(): TextMapPropagator = createW3CBaggagePropagator()

    override fun w3cTraceContext(): TextMapPropagator = createW3CTraceContextPropagator()

    override fun b3(format: B3Format): TextMapPropagator = createB3Propagator(format)
}
