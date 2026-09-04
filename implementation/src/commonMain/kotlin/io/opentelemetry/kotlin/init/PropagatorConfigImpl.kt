package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.propagation.B3Propagator
import io.opentelemetry.kotlin.propagation.CompositeTextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapGetter
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapSetter
import io.opentelemetry.kotlin.propagation.W3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.W3CTraceContextPropagator
import kotlin.concurrent.Volatile

@OptIn(ExperimentalApi::class)
internal class PropagatorConfigImpl : PropagatorConfigDsl {

    private var configured: TextMapPropagator? = null

    @Volatile private var w3cTraceContextImpl: TextMapPropagator = NoopOpenTelemetry.propagator

    @Volatile private var b3SingleImpl: TextMapPropagator = NoopOpenTelemetry.propagator

    @Volatile private var b3MultiImpl: TextMapPropagator = NoopOpenTelemetry.propagator

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        val composite = CompositeTextMapPropagator(propagators.toList())
        configured = composite
        return composite
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = W3CBaggagePropagator
        return W3CBaggagePropagator
    }

    override fun w3cTraceContext(): TextMapPropagator {
        val forwarder = ForwardingPropagator { w3cTraceContextImpl }
        configured = forwarder
        return forwarder
    }

    override fun b3(format: B3Format): TextMapPropagator {
        val forwarder = when (format) {
            B3Format.SINGLE -> ForwardingPropagator { b3SingleImpl }
            B3Format.MULTI -> ForwardingPropagator { b3MultiImpl }
        }
        configured = forwarder
        return forwarder
    }

    override fun none(): TextMapPropagator {
        val noop = NoopOpenTelemetry.propagator
        configured = noop
        return noop
    }

    // Factories are constructed after user config is applied, so we install them once available.
    internal fun installFactories(
        traceFlagsFactory: TraceFlagsFactory,
        traceStateFactory: TraceStateFactory,
        spanContextFactory: SpanContextFactory,
        spanFactory: SpanFactory,
        sdkErrorHandler: SdkErrorHandler,
    ) {
        w3cTraceContextImpl = W3CTraceContextPropagator(
            traceFlagsFactory = traceFlagsFactory,
            traceStateFactory = traceStateFactory,
            spanContextFactory = spanContextFactory,
            spanFactory = spanFactory,
            sdkErrorHandler = sdkErrorHandler,
        )
        b3SingleImpl = B3Propagator(
            B3Format.SINGLE,
            traceFlagsFactory,
            traceStateFactory,
            spanContextFactory,
            spanFactory,
            sdkErrorHandler,
        )
        b3MultiImpl = B3Propagator(
            B3Format.MULTI,
            traceFlagsFactory,
            traceStateFactory,
            spanContextFactory,
            spanFactory,
            sdkErrorHandler,
        )
    }

    internal fun buildPropagator(): TextMapPropagator = configured ?: defaultPropagator()

    /**
     * The `tracecontext,baggage` default mandated by the OTel spec.
     *
     * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration
     */
    private fun defaultPropagator(): TextMapPropagator = CompositeTextMapPropagator(
        listOf(ForwardingPropagator { w3cTraceContextImpl }, W3CBaggagePropagator)
    )
}

@OptIn(ExperimentalApi::class)
private class ForwardingPropagator(
    private val delegate: () -> TextMapPropagator,
) : TextMapPropagator {
    override fun fields(): Collection<String> = delegate().fields()

    override fun <T> inject(context: Context, carrier: T?, setter: TextMapSetter<T>) =
        delegate().inject(context, carrier, setter)

    override fun <T> extract(context: Context, carrier: T?, getter: TextMapGetter<T>): Context =
        delegate().extract(context, carrier, getter)
}
