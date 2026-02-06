package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider
import kotlinx.coroutines.withTimeout

@OptIn(ExperimentalApi::class)
internal class CloseableOpenTelemetryImpl(
    override val tracerProvider: TracerProvider,
    override val loggerProvider: LoggerProvider,
    override val clock: Clock,
    private val sdkFactory: SdkFactory,
    private val timeoutMs: Long = 3000,
) : OpenTelemetry, SdkFactory by sdkFactory, TelemetryCloseable {

    override suspend fun forceFlush(): OperationResultCode = withOverallTimeout {
        val tracerResult = when (tracerProvider) {
            is TelemetryCloseable -> tracerProvider.forceFlush()
            else -> Success
        }
        val loggerResult = when (loggerProvider) {
            is TelemetryCloseable -> loggerProvider.forceFlush()
            else -> Success
        }
        combineResults(tracerResult, loggerResult)
    }

    override suspend fun shutdown(): OperationResultCode = withOverallTimeout {
        val tracerResult = when (tracerProvider) {
            is TelemetryCloseable -> tracerProvider.shutdown()
            else -> Success
        }
        val loggerResult = when (loggerProvider) {
            is TelemetryCloseable -> loggerProvider.shutdown()
            else -> Success
        }
        combineResults(tracerResult, loggerResult)
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
