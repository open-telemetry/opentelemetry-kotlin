package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.config.ConfigProvider
import io.opentelemetry.kotlin.config.NoopConfigProvider
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.NoopContextFactory
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.logging.NoopLoggerProvider
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.metrics.NoopMeterProvider
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.createNoopPropagator
import io.opentelemetry.kotlin.tracing.NoopTracerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider

@ExperimentalApi
internal object NoopOpenTelemetryImpl : OpenTelemetrySdk {
    override val tracerProvider: TracerProvider = NoopTracerProvider
    override val loggerProvider: LoggerProvider = NoopLoggerProvider
    override val meterProvider: MeterProvider = NoopMeterProvider
    override val configProvider: ConfigProvider = NoopConfigProvider
    override val clock: Clock = NoopClock
    override val context: ContextFactory = NoopContextFactory
    override val propagator: TextMapPropagator = createNoopPropagator()

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
