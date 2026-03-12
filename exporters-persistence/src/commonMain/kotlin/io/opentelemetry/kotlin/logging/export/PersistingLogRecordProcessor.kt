package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

/**
 * Creates a processor that persists telemetry before exporting it. This effectively glues
 * together an existing processor/exporter chain so that a log record is always:
 *
 * 1. Mutated with any existing processors
 * 2. Batched into a suitable number of telemetry items
 * 3. The batch is written to disk by [PersistingLogRecordExporter]
 * 4. A periodic flush loop reads persisted records and exports them via the real exporter,
 *    deleting each record only after a successful export. Records from previous process launches
 *    are picked up automatically on the next flush.
 */
internal class PersistingLogRecordProcessor(
    processor: LogRecordProcessor,
    private val exporter: LogRecordExporter,
    fileSystem: TelemetryFileSystem,
    dsl: LogExportConfigDsl,
    config: PersistedTelemetryConfig,
    serializer: (List<ReadableLogRecord>) -> ByteArray,
    deserializer: (ByteArray) -> List<ReadableLogRecord>,
    maxQueueSize: Int,
    private val scheduleDelayMs: Long,
    private val exportTimeoutMs: Long,
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

    private val storingExporter = PersistingLogRecordExporter(exporter, repository)

    private val batchingProcessor = dsl.batchLogRecordProcessor(
        storingExporter,
        maxQueueSize,
        scheduleDelayMs,
        exportTimeoutMs,
        maxExportBatchSize,
        dispatcher,
    )

    private val composite = dsl.compositeLogRecordProcessor(processor, batchingProcessor)
    private val telemetryCloseable: TelemetryCloseable = TimeoutTelemetryCloseable(composite)

    private val flushMutex = Mutex()
    private val flushScope = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        flushScope.launch {
            while (!shutdownState.isShutdown) {
                delay(scheduleDelayMs)
                flushPersisted()
            }
        }
    }

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

    override suspend fun forceFlush(): OperationResultCode {
        if (shutdownState.isShutdown) {
            return Success
        }
        val result = telemetryCloseable.forceFlush()
        flushPersisted()
        return result
    }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            flushScope.cancel()
            val result = telemetryCloseable.shutdown()
            flushPersisted()
            exporter.shutdown()
            result
        }

    private suspend fun flushPersisted() {
        flushMutex.withLock {
            repository.listAll().forEach { record ->
                val telemetry = repository.read(record)

                // delete bad data
                if (telemetry == null) {
                    repository.delete(record)
                    return@forEach
                }
                val result = try {
                    withTimeout(exportTimeoutMs) { exporter.export(telemetry) }
                } catch (e: Throwable) {
                    Failure
                }
                if (result == Success) {
                    repository.delete(record)
                }
            }
        }
    }
}
