package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaSpanBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.factory.ContextFactory

internal class OtelJavaTracerAdapter(
    private val tracer: Tracer,
    private val contextFactory: ContextFactory,
) : OtelJavaTracer {
    override fun spanBuilder(spanName: String): OtelJavaSpanBuilder =
        OtelJavaSpanBuilderAdapter(tracer, spanName, contextFactory)
}
