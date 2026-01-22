package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler

@OptIn(ExperimentalApi::class)
internal class CompositeTelemetryCloseable(
    private val closeables: List<TelemetryCloseable>,
    private val sdkErrorHandler: SdkErrorHandler,
) : TelemetryCloseable {

    override suspend fun forceFlush(): OperationResultCode =
        batchExportOperationSuspend(
            closeables,
            sdkErrorHandler,
            TelemetryCloseable::forceFlush
        )

    override suspend fun shutdown(): OperationResultCode =
        batchExportOperationSuspend(
            closeables,
            sdkErrorHandler,
            TelemetryCloseable::shutdown
        )
}
