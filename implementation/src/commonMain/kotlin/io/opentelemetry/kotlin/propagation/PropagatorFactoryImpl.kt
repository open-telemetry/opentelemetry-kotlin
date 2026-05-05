package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory

@OptIn(ExperimentalApi::class)
internal class PropagatorFactoryImpl(
    private val traceFlagsFactory: TraceFlagsFactory,
    private val traceStateFactory: TraceStateFactory,
    private val spanContextFactory: SpanContextFactory,
    private val spanFactory: SpanFactory,
    private val contextFactory: ContextFactory,
) : PropagatorFactory {

    private val traceContextPropagator: TextMapPropagator by lazy {
        W3CTraceContextPropagator(
            traceFlagsFactory = traceFlagsFactory,
            traceStateFactory = traceStateFactory,
            spanContextFactory = spanContextFactory,
            spanFactory = spanFactory,
            contextFactory = contextFactory,
        )
    }

    override fun composite(vararg propagators: TextMapPropagator): TextMapPropagator {
        return composite(propagators.toList())
    }

    override fun composite(propagators: List<TextMapPropagator>): TextMapPropagator {
        return when (propagators.size) {
            0 -> EmptyTextMapPropagator
            1 -> propagators.single()
            else -> CompositeTextMapPropagator(propagators.toList())
        }
    }

    override fun w3cBaggage(): TextMapPropagator = W3CBaggagePropagator

    override fun w3cTraceContext(): TextMapPropagator = traceContextPropagator

    private object EmptyTextMapPropagator : TextMapPropagator {
        override fun fields(): Collection<String> = emptyList()
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {}
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = context
    }
}
