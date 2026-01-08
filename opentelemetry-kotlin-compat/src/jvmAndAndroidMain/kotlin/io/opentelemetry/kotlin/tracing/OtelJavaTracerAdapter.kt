package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaTracer

@OptIn(ExperimentalApi::class)
internal class OtelJavaTracerAdapter(
    private val tracer: Tracer
) : OtelJavaTracer {
    override fun spanBuilder(spanName: String): OtelJavaSpanBuilder = OtelJavaSpanBuilderAdapter(tracer, spanName)
}
