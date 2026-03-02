package io.opentelemetry.kotlin.factory
internal object NoopSdkFactory : SdkFactory {
    override val spanContextFactory: SpanContextFactory = NoopSpanContextFactory
    override val traceFlagsFactory: TraceFlagsFactory = NoopTraceFlagsFactory
    override val traceStateFactory: TraceStateFactory = NoopTraceStateFactory
    override val contextFactory: ContextFactory = NoopContextFactory
    override val spanFactory: SpanFactory = NoopSpanFactory
    override val tracingIdFactory: TracingIdFactory = NoopTracingIdFactory
}
