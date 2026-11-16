package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpFileExporter
import io.opentelemetry.kotlin.export.OtlpResponse
import io.opentelemetry.kotlin.export.TelemetryExporter
import io.opentelemetry.kotlin.ioDispatcher
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.encode.JsonLogRecordEncoder
import okio.blackholeSink
import okio.buffer
import kotlin.coroutines.CoroutineContext

/**
 * A [LogRecordExporter] that outputs log records in JSON format to a file.
 */
internal class OtlpFileLogRecordExporter(
    private val fileExporter: OtlpFileExporter = OtlpFileExporter(sink = blackholeSink().buffer()),
    private val encoder: JsonLogRecordEncoder = JsonLogRecordEncoder(),
    initialDelayMs: Long,
    maxAttemptIntervalMs: Long,
    maxAttempts: Int,
    private val sdkErrorHandler: SdkErrorHandler,
    coroutineContext: CoroutineContext = ioDispatcher
) : LogRecordExporter {

    private val exporter = TelemetryExporter<LogRecordData>(
        initialDelayMs,
        maxAttemptIntervalMs,
        maxAttempts,
        sdkErrorHandler,
        coroutineContext
    ) { telemetry ->
        val encodedTelemetry = sdkErrorHandler.sdkGuardOrDefault(null, "OTLP file encoding failed") {
            telemetry.map { encoder.encode(it) }
        }
        if (encodedTelemetry == null) {
            OtlpResponse.EncodingFailed
        } else {
            fileExporter.exportTelemetry(encodedTelemetry)
        }
    }

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
