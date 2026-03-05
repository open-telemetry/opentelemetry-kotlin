package io.opentelemetry.kotlin.factory
internal class SdkFactoryImpl(
    val idGenerator: IdGenerator = IdGeneratorImpl()
) : SdkFactory {
    override val traceFlags: TraceFlagsFactory by lazy { TraceFlagsFactoryImpl() }
    override val traceState: TraceStateFactory by lazy { TraceStateFactoryImpl() }
    override val spanContext: SpanContextFactory by lazy {
        SpanContextFactoryImpl(idGenerator, traceFlags, traceState)
    }
    override val context: ContextFactory by lazy { ContextFactoryImpl() }
    override val span: SpanFactory by lazy {
        SpanFactoryImpl(spanContext, (context as ContextFactoryImpl).spanKey)
    }
}
