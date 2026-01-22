package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.TelemetryExporter
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@OptIn(ExperimentalApi::class)
internal class OtlpHttpLogRecordExporter(
    private val otlpClient: OtlpClient,
    initialDelayMs: Long,
    maxAttemptIntervalMs: Long,
    maxAttempts: Int,
) : LogRecordExporter {

    private val exporter = TelemetryExporter(initialDelayMs, maxAttemptIntervalMs, maxAttempts) {
        otlpClient.exportLogs(it).also { response ->
            println("OTLP exported log: $response")
        }
    }

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
