package io.opentelemetry.example

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalApi::class)
internal class ExampleSpanProcessor : SpanProcessor {

    private val exporter: ExampleSpanExporter = ExampleSpanExporter()
    private val scope = CoroutineScope(Job())

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
    }

    override fun onEnding(span: ReadWriteSpan) {
    }

    override fun onEnd(span: ReadableSpan) {
        scope.launch {
            exporter.export(mutableListOf(span.toSpanData()))
        }
    }

    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
