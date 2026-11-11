package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.config.ConfigProvider
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.internalUninstallContextStorage
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
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

internal class OpenTelemetryImpl(
    override val tracerProvider: TracerProvider,
    override val loggerProvider: LoggerProvider,
    override val meterProvider: MeterProvider,
    override val configProvider: ConfigProvider,
    override val clock: Clock,
    override val context: ContextFactory,
    override val idGenerator: IdGenerator,
    override val resource: ResourceFactory,
    override val propagator: TextMapPropagator,
    private val installedStorage: CurrentContextStorage? = null,
    private val ownsErrorSink: Boolean = false,
    private val timeoutMs: Long = 3000,
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) : OpenTelemetrySdk {

    private val shutdownState: MutableShutdownState = MutableShutdownState()

    private val closeables = listOfNotNull(
        tracerProvider as? TelemetryCloseable,
        loggerProvider as? TelemetryCloseable,
        meterProvider as? TelemetryCloseable,
    )

    override suspend fun forceFlush(): OperationResultCode =
        batchExportOperationConcurrent(closeables, sdkErrorHandler, timeoutMs, TelemetryCloseable::forceFlush)

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(timeoutMs) {
            try {
                batchExportOperationConcurrent(closeables, sdkErrorHandler, timeoutMs, TelemetryCloseable::shutdown)
            } finally {
                releaseProcessWideState()
            }
        }

    @OptIn(TempContextStandInApi::class)
    private fun releaseProcessWideState() {
        installedStorage?.let(::internalUninstallContextStorage)
        if (ownsErrorSink) {
            setProcessSdkErrorHandler(null)
        }
    }
}
