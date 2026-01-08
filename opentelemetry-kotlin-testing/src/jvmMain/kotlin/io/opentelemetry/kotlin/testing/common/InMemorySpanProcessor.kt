package io.opentelemetry.kotlin.testing.common

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor

internal class InMemorySpanProcessor(private val exporter: InMemorySpanExporter) : OtelJavaSpanProcessor {
    override fun onStart(parentContext: OtelJavaContext, span: OtelJavaReadWriteSpan) {
    }

    override fun isStartRequired(): Boolean = true

    override fun onEnd(span: OtelJavaReadableSpan) {
        exporter.export(mutableListOf(span.toSpanData()))
    }

    override fun isEndRequired(): Boolean = true
}
