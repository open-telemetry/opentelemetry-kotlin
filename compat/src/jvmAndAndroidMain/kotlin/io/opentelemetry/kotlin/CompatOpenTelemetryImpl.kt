package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.config.ConfigProvider
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperationConcurrent
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.tracing.TracerProvider

internal class CompatOpenTelemetryImpl(
    override val tracerProvider: TracerProvider,
    override val loggerProvider: LoggerProvider,
    override val meterProvider: MeterProvider,
    override val clock: Clock,
    override val context: ContextFactory,
    override val idGenerator: IdGenerator,
    override val resource: ResourceFactory,
    override val propagator: TextMapPropagator,
    private val sdkErrorHandler: SdkErrorHandler,
    private val processWideState: CompatProcessWideState? = null,
    private val timeoutMs: Long = COMPAT_DEFAULT_TIMEOUT_MS,
) : OpenTelemetrySdk {

    // TODO: delegate to opentelemetry-java once it ships the Instrumentation Configuration API.
    override val configProvider: ConfigProvider = NoopOpenTelemetry.configProvider

    private val shutdownState: MutableShutdownState = MutableShutdownState()

    private val closeables = listOfNotNull(
        tracerProvider as? TelemetryCloseable,
        loggerProvider as? TelemetryCloseable,
        meterProvider as? TelemetryCloseable,
    )

    override suspend fun forceFlush(): OperationResultCode = when {
        shutdownState.isShutdown -> OperationResultCode.Success
        else -> batchExportOperationConcurrent(closeables, sdkErrorHandler, timeoutMs, TelemetryCloseable::forceFlush)
    }

    override suspend fun shutdown(): OperationResultCode = shutdownState.shutdown(timeoutMs) {
        try {
            batchExportOperationConcurrent(closeables, sdkErrorHandler, timeoutMs, TelemetryCloseable::shutdown)
        } finally {
            processWideState?.release()
        }
    }
}
