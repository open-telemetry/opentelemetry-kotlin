package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.BatchTelemetryProcessor
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

@OptIn(ExperimentalApi::class)
internal class BatchLogRecordProcessorImpl(
    private val exporter: LogRecordExporter,
    private val maxQueueSize: Int,
    private val scheduleDelayMs: Long,
    private val exportTimeoutMs: Long,
    private val maxExportBatchSize: Int,
) : LogRecordProcessor {

    private val processor =
        BatchTelemetryProcessor(
            maxQueueSize = maxQueueSize,
            scheduleDelayMs = scheduleDelayMs,
            exportTimeoutMs = exportTimeoutMs,
            maxExportBatchSize = maxExportBatchSize,
            exportAction = exporter::export
        )

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) = processor.processTelemetry(log)

    override suspend fun forceFlush(): OperationResultCode = processor.forceFlush()
    override suspend fun shutdown(): OperationResultCode = processor.shutdown()
}
