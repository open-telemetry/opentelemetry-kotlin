package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanAdapter
import io.opentelemetry.kotlin.tracing.model.ReadableSpanAdapter

@OptIn(ExperimentalApi::class)
internal class OtelJavaSpanProcessorAdapter(
    private val impl: SpanProcessor
) : OtelJavaSpanProcessor {

    override fun onStart(parentContext: OtelJavaContext, span: OtelJavaReadWriteSpan) {
        impl.onStart(ReadWriteSpanAdapter(span), parentContext.toOtelKotlinContext())
    }

    override fun onEnd(span: OtelJavaReadableSpan) {
        impl.onEnd(ReadableSpanAdapter(span))
    }

    override fun isStartRequired(): Boolean = impl.isStartRequired()
    override fun isEndRequired(): Boolean = impl.isEndRequired()
}
