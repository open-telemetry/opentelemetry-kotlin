package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.BaggageFactory
import io.opentelemetry.kotlin.factory.FakeBaggageFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.factory.FakeTraceStateFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.Propagators
import io.opentelemetry.kotlin.propagation.TextMapPropagator

public class FakePropagators(private val noneImpl: TextMapPropagator = FakeTextMapPropagator()) : Propagators {
    override val spanContext: SpanContextFactory = FakeSpanContextFactory()
    override val traceFlags: TraceFlagsFactory = FakeTraceFlagsFactory()
    override val traceState: TraceStateFactory = FakeTraceStateFactory()
    override val baggage: BaggageFactory = FakeBaggageFactory()
    override fun none(): TextMapPropagator = noneImpl
}
