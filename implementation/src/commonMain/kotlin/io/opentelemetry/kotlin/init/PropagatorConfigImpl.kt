package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.context.Context
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

    private var configured: TextMapPropagator = NoopOpenTelemetry.propagator

    @Volatile private var w3cTraceContextImpl: TextMapPropagator = NoopOpenTelemetry.propagator

    @Volatile private var b3SingleImpl: TextMapPropagator = NoopOpenTelemetry.propagator

    @Volatile private var b3MultiImpl: TextMapPropagator = NoopOpenTelemetry.propagator

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        configured = CompositeTextMapPropagator(propagators.toList())
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = W3CBaggagePropagator
        return configured
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

    // Factories are constructed after user config is applied, so we install them once available.
    internal fun installFactories(
        traceFlagsFactory: TraceFlagsFactory,
        traceStateFactory: TraceStateFactory,
        spanContextFactory: SpanContextFactory,
        spanFactory: SpanFactory,
    ) {
        w3cTraceContextImpl = W3CTraceContextPropagator(
            traceFlagsFactory = traceFlagsFactory,
            traceStateFactory = traceStateFactory,
            spanContextFactory = spanContextFactory,
            spanFactory = spanFactory,
        )
        b3SingleImpl = B3Propagator(B3Format.SINGLE, traceFlagsFactory, traceStateFactory, spanContextFactory, spanFactory)
        b3MultiImpl = B3Propagator(B3Format.MULTI, traceFlagsFactory, traceStateFactory, spanContextFactory, spanFactory)
    }

    internal fun buildPropagator(): TextMapPropagator = configured
}

@OptIn(ExperimentalApi::class)
private class ForwardingPropagator(
    private val delegate: () -> TextMapPropagator,
) : TextMapPropagator {
    override fun fields(): Collection<String> = delegate().fields()

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) =
        delegate().inject(context, carrier, setter)

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context =
        delegate().extract(context, carrier, getter)
}
