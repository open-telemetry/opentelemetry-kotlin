package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.BaggageFactory
import io.opentelemetry.kotlin.factory.NoopBaggageFactory
import io.opentelemetry.kotlin.factory.NoopSpanContextFactory
import io.opentelemetry.kotlin.factory.NoopTraceFlagsFactory
import io.opentelemetry.kotlin.factory.NoopTraceStateFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.propagation.NoopTextMapPropagator
import io.opentelemetry.kotlin.propagation.Propagators
import io.opentelemetry.kotlin.propagation.TextMapPropagator

internal object NoopPropagators : Propagators {
    override val spanContext: SpanContextFactory = NoopSpanContextFactory
    override val traceFlags: TraceFlagsFactory = NoopTraceFlagsFactory
    override val traceState: TraceStateFactory = NoopTraceStateFactory
    override val baggage: BaggageFactory = NoopBaggageFactory
    override fun none(): TextMapPropagator = NoopTextMapPropagator
}
