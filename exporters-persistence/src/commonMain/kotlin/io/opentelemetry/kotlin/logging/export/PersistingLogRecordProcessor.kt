package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.PersistedTelemetryConfig
import io.opentelemetry.kotlin.export.PersistedTelemetryType
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.TelemetryFileSystem
import io.opentelemetry.kotlin.export.TelemetryRepositoryImpl
import io.opentelemetry.kotlin.export.TimeoutTelemetryCloseable
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
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
@OptIn(ExperimentalApi::class)
internal class PersistingLogRecordProcessor(
    processors: List<LogRecordProcessor>,
    exporters: List<LogRecordExporter>,
    fileSystem: TelemetryFileSystem,
    clock: Clock,
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

    private val repository = TelemetryRepositoryImpl(
        type = PersistedTelemetryType.LOGS,
        config = config,
        fileSystem = fileSystem,
        serializer = serializer,
        deserializer = deserializer,
        clock = clock,
    )

    private val exporter = PersistingLogRecordExporter(exporters, repository)

    @Suppress("DEPRECATION")
    private val batchingProcessor = createBatchLogRecordProcessor(
        exporter,
        maxQueueSize,
        scheduleDelayMs,
        exportTimeoutMs,
        maxExportBatchSize,
        dispatcher,
    )

    @Suppress("DEPRECATION")
    private val processor = createCompositeLogRecordProcessor(processors + batchingProcessor)
    private val telemetryCloseable: TelemetryCloseable = TimeoutTelemetryCloseable(processor)

    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        try {
            processor.onEmit(log, context)
        } catch (e: Throwable) {
            sdkErrorHandler.onUserCodeError(
                e,
                "LogRecordProcessor.onEmit failed",
                SdkErrorSeverity.WARNING
            )
        }
    }

    override suspend fun forceFlush(): OperationResultCode = telemetryCloseable.forceFlush()
    override suspend fun shutdown(): OperationResultCode = telemetryCloseable.shutdown()
}
