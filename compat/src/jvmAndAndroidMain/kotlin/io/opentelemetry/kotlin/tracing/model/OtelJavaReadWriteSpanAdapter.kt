package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext

internal class OtelJavaReadWriteSpanAdapter(
    span: ReadWriteSpan,
    private val readableSpan: OtelJavaReadableSpan = OtelJavaReadableSpanAdapter(span),
) : OtelJavaReadWriteSpan,
    OtelJavaSpan by OtelJavaSpanAdapter(span),
    OtelJavaReadableSpan by readableSpan {

    override fun getSpanContext(): OtelJavaSpanContext = readableSpan.spanContext
}
