package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class CompatSdkFactory(
    override val tracingIdFactory: TracingIdFactory = CompatTracingIdFactory()
) : SdkFactory {
    override val spanContext: SpanContextFactory by lazy { CompatSpanContextFactory() }
    override val traceFlags: TraceFlagsFactory by lazy { CompatTraceFlagsFactory() }
    override val traceState: TraceStateFactory by lazy { CompatTraceStateFactory() }
    override val context: ContextFactory by lazy { CompatContextFactory() }
    override val span: SpanFactory by lazy { CompatSpanFactory(spanContext) }
}
