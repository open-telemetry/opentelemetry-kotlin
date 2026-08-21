package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
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
import kotlinx.coroutines.withTimeout

internal class OpenTelemetryImpl(
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
    private val timeoutMs: Long = 3000,
) : OpenTelemetrySdk {

    private val shutdownState: MutableShutdownState = MutableShutdownState()

    override suspend fun forceFlush(): OperationResultCode = withOverallTimeout {
        val tracerResult = when (tracerProvider) {
            is TelemetryCloseable -> tracerProvider.forceFlush()
            else -> Success
        }
        val loggerResult = when (loggerProvider) {
            is TelemetryCloseable -> loggerProvider.forceFlush()
            else -> Success
        }
        val meterResult = when (meterProvider) {
            is TelemetryCloseable -> meterProvider.forceFlush()
            else -> Success
        }
        combineResults(tracerResult, loggerResult, meterResult)
    }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            withOverallTimeout {
                val tracerResult = when (tracerProvider) {
                    is TelemetryCloseable -> tracerProvider.shutdown()
                    else -> Success
                }
                val loggerResult = when (loggerProvider) {
                    is TelemetryCloseable -> loggerProvider.shutdown()
                    else -> Success
                }
                val meterResult = when (meterProvider) {
                    is TelemetryCloseable -> meterProvider.shutdown()
                    else -> Success
                }
                combineResults(tracerResult, loggerResult, meterResult)
            }
        }

    private suspend fun withOverallTimeout(action: suspend () -> OperationResultCode): OperationResultCode =
        try {
            withTimeout(timeoutMs) { action() }
        } catch (_: Throwable) {
            Failure
        }

    private fun combineResults(vararg results: OperationResultCode): OperationResultCode =
        when {
            results.all { it == Success } -> Success
            else -> Failure
        }
}
