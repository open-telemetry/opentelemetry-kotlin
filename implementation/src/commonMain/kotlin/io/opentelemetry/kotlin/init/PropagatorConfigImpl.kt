package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.platformLog
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
    private val w3cTraceContext: DeferredW3CTraceContextPropagator = DeferredW3CTraceContextPropagator()

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        configured = CompositeTextMapPropagator(propagators.toList())
        return configured
    }

    override fun w3cBaggage(): TextMapPropagator {
        configured = W3CBaggagePropagator
        return configured
    }

    override fun w3cTraceContext(): TextMapPropagator {
        configured = w3cTraceContext
        return w3cTraceContext
    }

    // The W3C trace context propagator depends on factories that are constructed after user
    // config is applied, so we install them once they are available.
    internal fun installFactories(
        traceFlagsFactory: TraceFlagsFactory,
        traceStateFactory: TraceStateFactory,
        spanContextFactory: SpanContextFactory,
        spanFactory: SpanFactory,
    ) {
        w3cTraceContext.delegate = W3CTraceContextPropagator(
            traceFlagsFactory = traceFlagsFactory,
            traceStateFactory = traceStateFactory,
            spanContextFactory = spanContextFactory,
            spanFactory = spanFactory,
        )
    }

    internal fun buildPropagator(): TextMapPropagator = configured
}

@OptIn(ExperimentalApi::class)
private class DeferredW3CTraceContextPropagator : TextMapPropagator {

    @Volatile
    var delegate: TextMapPropagator? = null

    private var noDelegateWarningLogged = false

    override fun fields(): Collection<String> = resolveDelegate().fields()

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) =
        resolveDelegate().inject(context, carrier, setter)

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context =
        resolveDelegate().extract(context, carrier, getter)

    private fun resolveDelegate(): TextMapPropagator {
        val resolvedDelegate = delegate
        return if (resolvedDelegate != null) {
            resolvedDelegate
        } else {
            if (!noDelegateWarningLogged) {
                noDelegateWarningLogged = true
                platformLog("W3C trace context propagator accessed before SDK init completed")
            }
            NoopOpenTelemetry.propagator
        }
    }
}
