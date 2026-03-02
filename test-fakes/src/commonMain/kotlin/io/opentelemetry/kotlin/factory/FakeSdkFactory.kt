package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeSdkFactory : SdkFactory {
    override val spanContext: SpanContextFactory = FakeSpanContextFactory()
    override val traceFlags: TraceFlagsFactory = FakeTraceFlagsFactory()
    override val traceState: TraceStateFactory = FakeTraceStateFactory()
    override val context: ContextFactory = FakeContextFactory()
    override val span: SpanFactory = FakeSpanFactory()
    override val tracingIdFactory: TracingIdFactory = FakeTracingIdFactory()
}
