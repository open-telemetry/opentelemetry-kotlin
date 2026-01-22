package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OtlpClient
import io.opentelemetry.kotlin.export.TelemetryExporter
import io.opentelemetry.kotlin.tracing.data.SpanData

@OptIn(ExperimentalApi::class)
internal class OtlpHttpSpanExporter(
    private val otlpClient: OtlpClient,
    initialDelayMs: Long,
    maxAttemptIntervalMs: Long,
    maxAttempts: Int,
) : SpanExporter {

    private val exporter = TelemetryExporter(initialDelayMs, maxAttemptIntervalMs, maxAttempts) {
        otlpClient.exportTraces(it).also { response ->
            println("OTLP exported trace: $response")
        }
    }

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
