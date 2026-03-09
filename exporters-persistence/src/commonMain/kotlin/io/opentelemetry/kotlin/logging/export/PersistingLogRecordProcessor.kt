package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.PersistedTelemetryConfig
import io.opentelemetry.kotlin.export.PersistedTelemetryType
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.TelemetryFileSystem
import io.opentelemetry.kotlin.export.TelemetryRepositoryImpl
import io.opentelemetry.kotlin.export.TimeoutTelemetryCloseable
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Creates a processor that persists telemetry before exporting it. This effectively glues
 * together an existing processor/exporter chain so that a log record is always:
 *
 * 1. Mutated with any existing processors
 * 2. Batched into a suitable number of telemetry items
 * 3. The batch is passed to [PersistingLogRecordExporter], where it is written to disk
 * 4. [PersistingLogRecordExporter] then calls the existing export chain and deletes persisted
 * telemetry when it has been sent. [PersistingLogRecordExporter] is responsible for initiating
 * retries of unsent telemetry from previous process launches sent on disk.
 */
internal class PersistingLogRecordProcessor(
    processor: LogRecordProcessor,
    exporter: LogRecordExporter,
    fileSystem: TelemetryFileSystem,
    dsl: LogExportConfigDsl,
    config: PersistedTelemetryConfig,
    serializer: (List<ReadableLogRecord>) -> ByteArray,
    deserializer: (ByteArray) -> List<ReadableLogRecord>,
    maxQueueSize: Int,
    scheduleDelayMs: Long,
    exportTimeoutMs: Long,
    maxExportBatchSize: Int,
    private val sdkErrorHandler: SdkErrorHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : LogRecordProcessor {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val repository = TelemetryRepositoryImpl(
        type = PersistedTelemetryType.LOGS,
        config = config,
        fileSystem = fileSystem,
        serializer = serializer,
        deserializer = deserializer,
        clock = dsl.clock,
    )

    private val persistingExporter = PersistingLogRecordExporter(exporter, repository)

    private val batchingProcessor = dsl.batchLogRecordProcessor(
        persistingExporter,
        maxQueueSize,
        scheduleDelayMs,
        exportTimeoutMs,
        maxExportBatchSize,
        dispatcher,
    )

    private val composite = dsl.compositeLogRecordProcessor(processor, batchingProcessor)
    private val telemetryCloseable: TelemetryCloseable = TimeoutTelemetryCloseable(composite)

    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        shutdownState.execute {
            try {
                composite.onEmit(log, context)
            } catch (e: Throwable) {
                sdkErrorHandler.onUserCodeError(
                    e,
                    "LogRecordProcessor.onEmit failed",
                    SdkErrorSeverity.WARNING
                )
            }
        }
    }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = !shutdownState.isShutdown

    override suspend fun forceFlush(): OperationResultCode = telemetryCloseable.forceFlush()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            telemetryCloseable.shutdown()
        }
}
