package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.BatchTelemetryConfig
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.BatchTelemetryProcessor
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class BatchLogRecordProcessorImpl(
    private val exporter: LogRecordExporter,
    maxQueueSize: Int,
    scheduleDelayMs: Long,
    exportTimeoutMs: Long,
    maxExportBatchSize: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : LogRecordProcessor {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val processor =
        BatchTelemetryProcessor(
            config = BatchTelemetryConfig(
                maxQueueSize = maxQueueSize,
                scheduleDelayMs = scheduleDelayMs,
                exportTimeoutMs = exportTimeoutMs,
                maxExportBatchSize = maxExportBatchSize,
            ),
            dispatcher = dispatcher,
            exportAction = exporter::export
        )

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) = shutdownState.execute { processor.processTelemetry(log) }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = !shutdownState.isShutdown

    override suspend fun forceFlush(): OperationResultCode = processor.forceFlush()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS) {
            val exporterResult = exporter.shutdown()
            processor.shutdown()
            exporterResult
        }
}
