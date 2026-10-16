package io.opentelemetry.kotlin.propagation

import io.opentelemetry.context.propagation.TextMapPropagator.composite
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.init.PropagatorConfigDsl

@OptIn(ExperimentalApi::class)
internal class CompatPropagatorConfigImpl : PropagatorConfigDsl {

    private var configured: TextMapPropagator = TextMapPropagatorAdapter(OtelJavaTextMapPropagator.noop())

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        val javaPropagators = propagators.map { it.toOtelJavaTextMapPropagator() }
        configured = TextMapPropagatorAdapter(composite(javaPropagators))
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = createW3CBaggagePropagator()
        return configured
    }

    override fun w3cTraceContext(): TextMapPropagator {
        configured = createW3CTraceContextPropagator()
        return configured
    }

    override fun b3(format: B3Format): TextMapPropagator {
        configured = createB3Propagator(format)
        return configured
    }

    internal fun buildPropagator(): TextMapPropagator = configured
}
