package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.TelemetryExporter
import io.opentelemetry.kotlin.ioDispatcher
import io.opentelemetry.kotlin.logging.data.LogRecordData
import kotlin.coroutines.CoroutineContext
import kotlin.time.TimeSource

internal class OtlpHttpLogRecordExporter(
    private val otlpClient: OtlpClient,
    initialDelayMs: Long,
    maxAttemptIntervalMs: Long,
    maxAttempts: Int,
    sdkErrorHandler: SdkErrorHandler,
    coroutineContext: CoroutineContext = ioDispatcher,
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) : LogRecordExporter {

    private val exporter = TelemetryExporter(
        initialDelayMs,
        maxAttemptIntervalMs,
        maxAttempts,
        sdkErrorHandler,
        coroutineContext,
        timeSource
    ) {
        otlpClient.exportLogs(it)
    }

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
