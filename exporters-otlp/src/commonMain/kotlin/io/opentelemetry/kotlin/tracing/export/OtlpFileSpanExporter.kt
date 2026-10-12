package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpFileExporter
import io.opentelemetry.kotlin.export.TelemetryExporter
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.encode.JsonSpanEncoder
import okio.blackholeSink
import okio.buffer

/**
 * A [SpanExporter] that outputs span data in JSON format to a file.
 */
internal class OtlpFileSpanExporter(
    private val fileExporter: OtlpFileExporter = OtlpFileExporter(blackholeSink().buffer()),
    private val encoder: JsonSpanEncoder = JsonSpanEncoder(),
    initialDelayMs: Long,
    maxAttemptIntervalMs: Long,
    maxAttempts: Int,
    sdkErrorHandler: SdkErrorHandler,
) : SpanExporter {

    private val exporter = TelemetryExporter(
        initialDelayMs,
        maxAttemptIntervalMs,
        maxAttempts,
        sdkErrorHandler,
    ) { telemetry ->
        val encodedTelemetry = telemetry.map { encoder.encode(it) }
        fileExporter.exportTelemetry(encodedTelemetry)
    }

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
