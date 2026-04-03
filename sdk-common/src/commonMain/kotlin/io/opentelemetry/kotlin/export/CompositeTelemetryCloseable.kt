package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Delegates [forceFlush] and [shutdown] to multiple [TelemetryCloseable]s,
 * returning [OperationResultCode.Success] only if every delegate succeeds.
 */
@ExperimentalApi
public class CompositeTelemetryCloseable(
    private val closeables: List<TelemetryCloseable>,
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) : TelemetryCloseable {

    override suspend fun forceFlush(): OperationResultCode =
        batchExportOperationSuspend(
            elements = closeables,
            sdkErrorHandler = sdkErrorHandler,
        ) {
            runWithTimeout(FORCE_FLUSH_TIMEOUT_MS) { it.forceFlush() }
        }

    override suspend fun shutdown(): OperationResultCode =
        batchExportOperationSuspend(
            elements = closeables,
            sdkErrorHandler = sdkErrorHandler,
        ) {
            runWithTimeout(SHUTDOWN_TIMEOUT_MS) { it.shutdown() }
        }

    private companion object {
        const val FORCE_FLUSH_TIMEOUT_MS = 5000L
        const val SHUTDOWN_TIMEOUT_MS = 5000L
    }
}
