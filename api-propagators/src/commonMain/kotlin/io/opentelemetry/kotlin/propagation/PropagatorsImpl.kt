package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.BaggageFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory

internal class PropagatorsImpl : Propagators {

    override fun none(): TextMapPropagator = NoopTextMapPropagator

    override val spanContext: SpanContextFactory
        get() = TODO("Not yet implemented")
    override val traceFlags: TraceFlagsFactory
        get() = TODO("Not yet implemented")
    override val traceState: TraceStateFactory
        get() = TODO("Not yet implemented")
    override val baggage: BaggageFactory
        get() = TODO("Not yet implemented")

    private object NoopTextMapPropagator : TextMapPropagator {
        override fun fields(): Collection<String> = emptyList()
        override fun <T> inject(context: Context, carrier: T?, setter: TextMapSetter<T>) {}
        override fun <T> extract(context: Context, carrier: T?, getter: TextMapGetter<T>): Context = context
    }
}
