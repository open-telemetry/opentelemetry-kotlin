package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.TelemetryExporter
import io.opentelemetry.kotlin.ioDispatcher
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlin.coroutines.CoroutineContext

internal class OtlpHttpSpanExporter(
    private val otlpClient: OtlpClient,
    initialDelayMs: Long,
    maxAttemptIntervalMs: Long,
    maxAttempts: Int,
    sdkErrorHandler: SdkErrorHandler,
    coroutineContext: CoroutineContext = ioDispatcher,
) : SpanExporter {

    private val exporter = TelemetryExporter(
        initialDelayMs,
        maxAttemptIntervalMs,
        maxAttempts,
        sdkErrorHandler,
        coroutineContext,
    ) {
        otlpClient.exportTraces(it)
    }

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
