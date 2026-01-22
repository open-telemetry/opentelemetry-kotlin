package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

@OptIn(ExperimentalApi::class)
internal class InMemorySpanProcessor(private val exporter: InMemorySpanExporter) : SpanProcessor {

    override fun onStart(span: ReadWriteSpan, parentContext: Context) {
    }

    override fun onEnding(span: ReadWriteSpan) {
    }

    override fun onEnd(span: ReadableSpan) {
        exporter.export(listOf(span.toSpanData()))
    }

    override fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override fun shutdown(): OperationResultCode = OperationResultCode.Success
    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true
}
