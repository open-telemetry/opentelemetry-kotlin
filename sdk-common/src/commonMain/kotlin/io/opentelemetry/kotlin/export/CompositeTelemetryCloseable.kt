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
            action = TelemetryCloseable::forceFlush
        )

    override suspend fun shutdown(): OperationResultCode =
        batchExportOperationSuspend(
            elements = closeables,
            sdkErrorHandler = sdkErrorHandler,
            action = TelemetryCloseable::shutdown
        )
}
