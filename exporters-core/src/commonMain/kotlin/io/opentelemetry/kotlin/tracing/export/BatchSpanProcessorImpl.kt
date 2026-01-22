package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.BatchTelemetryProcessor
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

@OptIn(ExperimentalApi::class)
internal class BatchSpanProcessorImpl(
    private val exporter: SpanExporter,
    private val maxQueueSize: Int,
    private val scheduleDelayMs: Long,
    private val exportTimeoutMs: Long,
    private val maxExportBatchSize: Int,
) : SpanProcessor {

    private val processor =
        BatchTelemetryProcessor(
            maxQueueSize = maxQueueSize,
            scheduleDelayMs = scheduleDelayMs,
            exportTimeoutMs = exportTimeoutMs,
            maxExportBatchSize = maxExportBatchSize,
            exportAction = exporter::export
        )

    override fun onEnd(span: ReadableSpan) = processor.processTelemetry(span)

    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
    }

    override fun onEnding(span: ReadWriteSpan) {
    }

    override fun forceFlush(): OperationResultCode = processor.forceFlush()
    override fun shutdown(): OperationResultCode = processor.shutdown()
}
