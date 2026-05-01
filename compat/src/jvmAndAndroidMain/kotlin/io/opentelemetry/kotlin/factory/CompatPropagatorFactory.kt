package io.opentelemetry.kotlin.factory

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.propagation.OtelJavaTextMapPropagatorAdapter
import io.opentelemetry.kotlin.propagation.PropagatorFactory
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapPropagatorAdapter

@OptIn(ExperimentalApi::class)
internal class CompatPropagatorFactory : PropagatorFactory {

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator =
        composite(propagators.toList())

    override fun composite(propagators: List<TextMapPropagator>): TextMapPropagator {
        val javaPropagators = propagators.map { it.toOtelJava() }
        return TextMapPropagatorAdapter(OtelJavaTextMapPropagator.composite(javaPropagators))
    }

    override fun w3cBaggage(): TextMapPropagator {
        return TextMapPropagatorAdapter(W3CBaggagePropagator.getInstance())
    }

    private fun TextMapPropagator.toOtelJava(): OtelJavaTextMapPropagator =
        (this as? TextMapPropagatorAdapter)?.impl ?: OtelJavaTextMapPropagatorAdapter(this)
}
