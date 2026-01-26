package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.threadSafeList

/**
 * Delegates to multiple [TelemetryCloseable]s.
 */
@OptIn(ExperimentalApi::class)
internal class DelegatingTelemetryCloseable(
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler
) : TelemetryCloseable {

    private val telemetryCloseables: MutableList<TelemetryCloseable> = threadSafeList()

    fun add(telemetryCloseable: TelemetryCloseable) {
        telemetryCloseables.add(telemetryCloseable)
    }

    override suspend fun forceFlush(): OperationResultCode {
        return executeOnAll(TelemetryCloseable::forceFlush)
    }

    override suspend fun shutdown(): OperationResultCode {
        return executeOnAll(TelemetryCloseable::shutdown)
    }

    private suspend fun executeOnAll(
        action: suspend (TelemetryCloseable) -> OperationResultCode
    ): OperationResultCode {
        var success = true
        val snapshot = telemetryCloseables.toList()

        snapshot.forEach { closeable ->
            try {
                val result = action(closeable)
                success = success && result == OperationResultCode.Success
            } catch (exc: Throwable) {
                success = false
                sdkErrorHandler.onUserCodeError(
                    exc,
                    "Export operation failed",
                    SdkErrorSeverity.WARNING
                )
            }
        }
        return when {
            success -> OperationResultCode.Success
            else -> OperationResultCode.Failure
        }
    }
}
