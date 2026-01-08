package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeSdkFactory : SdkFactory {
    override val spanContextFactory: SpanContextFactory = FakeSpanContextFactory()
    override val traceFlagsFactory: TraceFlagsFactory = FakeTraceFlagsFactory()
    override val traceStateFactory: TraceStateFactory = FakeTraceStateFactory()
    override val contextFactory: ContextFactory = FakeContextFactory()
    override val spanFactory: SpanFactory = FakeSpanFactory()
    override val tracingIdFactory: TracingIdFactory = FakeTracingIdFactory()
}
