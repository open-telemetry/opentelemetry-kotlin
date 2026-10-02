package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpFileExporter
import io.opentelemetry.kotlin.export.TelemetryExporter
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.encode.JsonLogRecordEncoder
import okio.blackholeSink
import okio.buffer

/**
 * A [LogRecordExporter] that outputs log records in JSON format to a file.
 */
internal class OtlpFileLogRecordExporter(
    private val fileExporter: OtlpFileExporter = OtlpFileExporter(sink = blackholeSink().buffer()),
    private val encoder: JsonLogRecordEncoder = JsonLogRecordEncoder(),
    initialDelayMs: Long,
    maxAttemptIntervalMs: Long,
    maxAttempts: Int,
    sdkErrorHandler: SdkErrorHandler,
) : LogRecordExporter {

    private val exporter = TelemetryExporter(
        initialDelayMs,
        maxAttemptIntervalMs,
        maxAttempts,
        sdkErrorHandler,
    ) { telemetry ->
        val encodedTelemetry = telemetry.map { encoder.encode(it) }
        fileExporter.exportTelemetry(encodedTelemetry)
    }

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}