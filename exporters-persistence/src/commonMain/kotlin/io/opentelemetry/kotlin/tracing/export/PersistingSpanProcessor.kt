package io.opentelemetry.kotlin.tracing.export

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
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
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
 * together an existing processor/exporter chain so that a span is always:
 *
 * 1. Mutated with any existing processors
 * 2. Batched into a suitable number of telemetry items
 * 3. The batch is written to disk by [PersistingSpanExporter]
 * 4. A periodic flush loop reads persisted spans and exports them via the real exporter,
 *    deleting each span only after a successful export. Spans from previous process launches
 *    are picked up automatically on the next flush.
 */
internal class PersistingSpanProcessor(
    processor: SpanProcessor,
    private val exporter: SpanExporter,
    fileSystem: TelemetryFileSystem,
    dsl: TraceExportConfigDsl,
    config: PersistedTelemetryConfig,
    serializer: (List<SpanData>) -> ByteArray,
    deserializer: (ByteArray) -> List<SpanData>,
    maxQueueSize: Int,
    private val scheduleDelayMs: Long,
    private val exportTimeoutMs: Long,
    maxExportBatchSize: Int,
    private val sdkErrorHandler: SdkErrorHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SpanProcessor {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val repository = TelemetryRepositoryImpl(
        type = PersistedTelemetryType.SPANS,
        config = config,
        fileSystem = fileSystem,
        serializer = serializer,
        deserializer = deserializer,
        clock = dsl.clock,
    )

    private val persistingExporter = PersistingSpanExporter(exporter, repository)

    private val batchingProcessor = dsl.batchSpanProcessor(
        persistingExporter,
        maxQueueSize,
        scheduleDelayMs,
        exportTimeoutMs,
        maxExportBatchSize,
        dispatcher,
    )

    private val composite = dsl.compositeSpanProcessor(processor, batchingProcessor)
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

    override fun onStart(span: ReadWriteSpan, parentContext: Context) = shutdownState.execute {
        try {
            composite.onStart(span, parentContext)
        } catch (e: Throwable) {
            sdkErrorHandler.onUserCodeError(
                e,
                "SpanProcessor.onStart failed",
                SdkErrorSeverity.WARNING
            )
        }
    }

    override fun onEnding(span: ReadWriteSpan) = shutdownState.execute {
        try {
            composite.onEnding(span)
        } catch (e: Throwable) {
            sdkErrorHandler.onUserCodeError(
                e,
                "SpanProcessor.onEnding failed",
                SdkErrorSeverity.WARNING
            )
        }
    }

    override fun onEnd(span: ReadableSpan) = shutdownState.execute {
        try {
            composite.onEnd(span)
        } catch (e: Throwable) {
            sdkErrorHandler.onUserCodeError(
                e,
                "SpanProcessor.onEnd failed",
                SdkErrorSeverity.WARNING
            )
        }
    }

    override fun isStartRequired(): Boolean = composite.isStartRequired()
    override fun isEndRequired(): Boolean = composite.isEndRequired()

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
