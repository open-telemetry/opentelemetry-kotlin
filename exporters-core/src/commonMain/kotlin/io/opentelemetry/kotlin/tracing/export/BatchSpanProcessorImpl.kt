package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.BatchTelemetryProcessor
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class BatchSpanProcessorImpl(
    private val exporter: SpanExporter,
    private val maxQueueSize: Int,
    private val scheduleDelayMs: Long,
    private val exportTimeoutMs: Long,
    private val maxExportBatchSize: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SpanProcessor {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val processor =
        BatchTelemetryProcessor(
            maxQueueSize = maxQueueSize,
            scheduleDelayMs = scheduleDelayMs,
            exportTimeoutMs = exportTimeoutMs,
            maxExportBatchSize = maxExportBatchSize,
            dispatcher = dispatcher,
            exportAction = exporter::export
        )

    override fun onEnd(span: ReadableSpan) = shutdownState.execute { processor.processTelemetry(span) }

    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
    }

    override fun onEnding(span: ReadWriteSpan) {
    }

    override suspend fun forceFlush(): OperationResultCode = processor.forceFlush()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            exporter.shutdown()
            processor.shutdown()
        }
}
