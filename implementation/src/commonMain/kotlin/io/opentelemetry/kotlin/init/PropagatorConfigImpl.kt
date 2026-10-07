package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.propagation.B3Propagator
import io.opentelemetry.kotlin.propagation.TextMapGetter
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapSetter
import io.opentelemetry.kotlin.propagation.W3CTraceContextPropagator
import io.opentelemetry.kotlin.propagation.createCompositePropagator
import io.opentelemetry.kotlin.propagation.createNoopPropagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator
import kotlin.concurrent.Volatile

@OptIn(ExperimentalApi::class)
internal class PropagatorConfigImpl : PropagatorConfigDsl {

    private val none: TextMapPropagator = createNoopPropagator()

    private var configured: TextMapPropagator = none

    @Volatile private var w3cTraceContextImpl: TextMapPropagator = none

    @Volatile private var b3SingleImpl: TextMapPropagator = none

    @Volatile private var b3MultiImpl: TextMapPropagator = none

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        configured = createCompositePropagator(*propagators)
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = createW3CBaggagePropagator()
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
    internal fun installFactories(spanFactory: SpanFactory) {
        w3cTraceContextImpl = W3CTraceContextPropagator(spanFactory = spanFactory)
        b3SingleImpl = B3Propagator(B3Format.SINGLE, spanFactory)
        b3MultiImpl = B3Propagator(B3Format.MULTI, spanFactory)
    }

    internal fun buildPropagator(): TextMapPropagator = configured
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
