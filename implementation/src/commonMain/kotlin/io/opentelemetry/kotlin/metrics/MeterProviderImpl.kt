package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.platformLog
import io.opentelemetry.kotlin.provider.ApiProviderImpl

internal class MeterProviderImpl(
    metricsConfig: MetricsConfig,
) : MeterProvider, TelemetryCloseable {

    private val sdkErrorHandler = metricsConfig.sdkErrorHandler
    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val closeable: TelemetryCloseable = CompositeTelemetryCloseable(emptyList(), sdkErrorHandler)
    private val noopMeter = NoopOpenTelemetry.meterProvider.getMeter("")

    private val apiProvider by lazy {
        ApiProviderImpl<Meter> { key ->
            MeterImpl(
                instrumentationScopeInfo = key,
                resource = metricsConfig.resource,
            )
        }
    }

    override fun getMeter(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?,
    ): Meter =
        sdkErrorHandler.guardOrDefault(noopMeter, "MeterProvider.getMeter failed") {
            shutdownState.ifActiveOrElse(noopMeter) {
                if (name.isEmpty()) {
                    platformLog("Meter requested without instrumentation scope name")
                }
                val key = apiProvider.createInstrumentationScopeInfo(name, version, schemaUrl, attributes)
                apiProvider.getOrCreate(key)
            }
        }

    override suspend fun forceFlush(): OperationResultCode =
        sdkErrorHandler.guardOrDefaultSuspend(OperationResultCode.Failure, "MeterProvider.forceFlush failed") {
            runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS, closeable::forceFlush)
        }

    override suspend fun shutdown(): OperationResultCode =
        sdkErrorHandler.guardOrDefaultSuspend(OperationResultCode.Failure, "MeterProvider.shutdown failed") {
            shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS, closeable::shutdown)
        }
}
