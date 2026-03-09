package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.BatchTelemetryProcessor
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class BatchLogRecordProcessorImpl(
    private val exporter: LogRecordExporter,
    private val maxQueueSize: Int,
    private val scheduleDelayMs: Long,
    private val exportTimeoutMs: Long,
    private val maxExportBatchSize: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : LogRecordProcessor {

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
        shutdownState.shutdown {
            exporter.shutdown()
            processor.shutdown()
        }
}
