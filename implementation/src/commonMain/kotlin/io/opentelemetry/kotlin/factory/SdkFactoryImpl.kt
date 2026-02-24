package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class SdkFactoryImpl(
    override val identifier: TracingIdFactory = TracingIdFactoryImpl()
) : SdkFactory {
    override val traceFlags: TraceFlagsFactory by lazy { TraceFlagsFactoryImpl() }
    override val traceState: TraceStateFactory by lazy { TraceStateFactoryImpl() }
    override val spanContext: SpanContextFactory by lazy {
        SpanContextFactoryImpl(identifier, traceFlags, traceState)
    }
    override val context: ContextFactory by lazy { ContextFactoryImpl() }
    override val span: SpanFactory by lazy {
        SpanFactoryImpl(spanContext, (context as ContextFactoryImpl).spanKey)
    }
}
