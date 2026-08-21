package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.BaggageFactory
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.tracing.TracerProvider

internal class CompatOpenTelemetryImpl(
    override val tracerProvider: TracerProvider,
    override val loggerProvider: LoggerProvider,
    override val meterProvider: MeterProvider,
    override val clock: Clock,
    override val spanContext: SpanContextFactory,
    override val traceFlags: TraceFlagsFactory,
    override val traceState: TraceStateFactory,
    override val context: ContextFactory,
    override val span: SpanFactory,
    override val baggage: BaggageFactory,
    override val idGenerator: IdGenerator,
    override val resource: ResourceFactory,
    override val propagator: TextMapPropagator,
    sdkErrorHandler: SdkErrorHandler,
) : OpenTelemetrySdk {

    private val shutdownState: MutableShutdownState = MutableShutdownState()

    private val closeable = CompositeTelemetryCloseable(
        closeables = listOfNotNull(
            tracerProvider as? TelemetryCloseable,
            loggerProvider as? TelemetryCloseable,
            meterProvider as? TelemetryCloseable,
        ),
        sdkErrorHandler = sdkErrorHandler,
    )

    override suspend fun forceFlush(): OperationResultCode = when {
        shutdownState.isShutdown -> OperationResultCode.Success
        else -> closeable.forceFlush()
    }

    override suspend fun shutdown(): OperationResultCode = shutdownState.shutdown { closeable.shutdown() }
}
