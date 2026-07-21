package io.opentelemetry.kotlin.propagation

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.TextMapPropagator.composite
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.init.PropagatorConfigDsl
import io.opentelemetry.extension.trace.propagation.B3Propagator as JavaB3Propagator

@OptIn(ExperimentalApi::class)
internal class CompatPropagatorConfigImpl : PropagatorConfigDsl {

    private var configured: TextMapPropagator = TextMapPropagatorAdapter(OtelJavaTextMapPropagator.noop())

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        val javaPropagators = propagators.map { it.toOtelJavaTextMapPropagator() }
        configured = TextMapPropagatorAdapter(composite(javaPropagators))
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = TextMapPropagatorAdapter(W3CBaggagePropagator.getInstance())
        return configured
    }

    override fun w3cTraceContext(): TextMapPropagator {
        configured = TextMapPropagatorAdapter(W3CTraceContextPropagator.getInstance())
        return configured
    }

    override fun b3(format: B3Format): TextMapPropagator {
        val javaPropagator = when (format) {
            B3Format.SINGLE -> JavaB3Propagator.injectingSingleHeader()
            B3Format.MULTI -> JavaB3Propagator.injectingMultiHeaders()
        }
        configured = TextMapPropagatorAdapter(javaPropagator)
        return configured
    }

    internal fun buildPropagator(): TextMapPropagator = configured

    private fun TextMapPropagator.toOtelJavaTextMapPropagator(): OtelJavaTextMapPropagator =
        (this as? TextMapPropagatorAdapter)?.impl ?: OtelJavaTextMapPropagatorAdapter(this)
}
