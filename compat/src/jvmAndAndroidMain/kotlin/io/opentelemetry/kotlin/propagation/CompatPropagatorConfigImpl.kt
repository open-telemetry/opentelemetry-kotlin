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

    private var configured: TextMapPropagator? = null

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        val javaPropagators = propagators.map { it.toOtelJavaTextMapPropagator() }
        return TextMapPropagatorAdapter(composite(javaPropagators)).also { configured = it }
    }

    override fun w3cBaggage(): TextMapPropagator =
        TextMapPropagatorAdapter(W3CBaggagePropagator.getInstance()).also { configured = it }

    override fun w3cTraceContext(): TextMapPropagator =
        TextMapPropagatorAdapter(W3CTraceContextPropagator.getInstance()).also { configured = it }

    override fun b3(format: B3Format): TextMapPropagator {
        val javaPropagator = when (format) {
            B3Format.SINGLE -> JavaB3Propagator.injectingSingleHeader()
            B3Format.MULTI -> JavaB3Propagator.injectingMultiHeaders()
        }
        return TextMapPropagatorAdapter(javaPropagator).also { configured = it }
    }

    override fun none(): TextMapPropagator =
        TextMapPropagatorAdapter(OtelJavaTextMapPropagator.noop()).also { configured = it }

    internal fun buildPropagator(): TextMapPropagator = configured ?: defaultPropagator()

    /**
     * The `tracecontext,baggage` default mandated by the OTel spec.
     *
     * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration
     */
    private fun defaultPropagator(): TextMapPropagator = TextMapPropagatorAdapter(
        composite(listOf(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance()))
    )

    private fun TextMapPropagator.toOtelJavaTextMapPropagator(): OtelJavaTextMapPropagator =
        (this as? TextMapPropagatorAdapter)?.impl ?: OtelJavaTextMapPropagatorAdapter(this)
}
