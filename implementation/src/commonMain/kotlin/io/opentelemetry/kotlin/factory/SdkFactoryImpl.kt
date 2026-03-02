package io.opentelemetry.kotlin.factory
internal class SdkFactoryImpl(
    override val tracingIdFactory: TracingIdFactory = TracingIdFactoryImpl()
) : SdkFactory {
    override val traceFlagsFactory: TraceFlagsFactory by lazy { TraceFlagsFactoryImpl() }
    override val traceStateFactory: TraceStateFactory by lazy { TraceStateFactoryImpl() }
    override val spanContextFactory: SpanContextFactory by lazy {
        SpanContextFactoryImpl(tracingIdFactory, traceFlagsFactory, traceStateFactory)
    }
    override val contextFactory: ContextFactory by lazy { ContextFactoryImpl() }
    override val spanFactory: SpanFactory by lazy {
        SpanFactoryImpl(spanContextFactory, (contextFactory as ContextFactoryImpl).spanKey)
    }
}
