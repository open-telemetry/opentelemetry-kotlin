package io.opentelemetry.kotlin.tracing.export

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
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Creates a processor that persists telemetry before exporting it. This effectively glues
 * together an existing processor/exporter chain so that a span is always:
 *
 * 1. Mutated with any existing processors
 * 2. Batched into a suitable number of telemetry items
 * 3. The batch is passed to [PersistingSpanExporter], where it is written to disk
 * 4. [PersistingSpanExporter] then calls the existing export chain and deletes persisted
 * telemetry when it has been sent. [PersistingSpanExporter] is responsible for initiating
 * retries of unsent telemetry from previous process launches sent on disk.
 */
@OptIn(ExperimentalApi::class)
internal class PersistingSpanProcessor(
    processor: SpanProcessor,
    exporter: SpanExporter,
    fileSystem: TelemetryFileSystem,
    clock: Clock,
    config: PersistedTelemetryConfig,
    serializer: (List<SpanData>) -> ByteArray,
    deserializer: (ByteArray) -> List<SpanData>,
    maxQueueSize: Int,
    scheduleDelayMs: Long,
    exportTimeoutMs: Long,
    maxExportBatchSize: Int,
    private val sdkErrorHandler: SdkErrorHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SpanProcessor {

    private val repository = TelemetryRepositoryImpl(
        type = PersistedTelemetryType.SPANS,
        config = config,
        fileSystem = fileSystem,
        serializer = serializer,
        deserializer = deserializer,
        clock = clock,
    )

    private val persistingExporter = PersistingSpanExporter(exporter, repository)

    @Suppress("DEPRECATION")
    private val batchingProcessor = createBatchSpanProcessor(
        persistingExporter,
        maxQueueSize,
        scheduleDelayMs,
        exportTimeoutMs,
        maxExportBatchSize,
        dispatcher,
    )

    @Suppress("DEPRECATION")
    private val composite = createCompositeSpanProcessor(listOf(processor, batchingProcessor))
    private val telemetryCloseable: TelemetryCloseable = TimeoutTelemetryCloseable(composite)

    override fun onStart(span: ReadWriteSpan, parentContext: Context) {
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

    override fun onEnding(span: ReadWriteSpan) {
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

    override fun onEnd(span: ReadableSpan) {
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

    override suspend fun forceFlush(): OperationResultCode = telemetryCloseable.forceFlush()
    override suspend fun shutdown(): OperationResultCode = telemetryCloseable.shutdown()
}
