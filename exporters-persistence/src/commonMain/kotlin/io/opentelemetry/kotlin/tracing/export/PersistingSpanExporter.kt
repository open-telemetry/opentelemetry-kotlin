package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.export.TelemetryRepository
import io.opentelemetry.kotlin.tracing.data.SpanData

@ExperimentalApi
internal class PersistingSpanExporter(
    private val exporter: SpanExporter,
    private val repository: TelemetryRepository<SpanData>,
) : SpanExporter {

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
        // if persistence failed attempt immediate export as a best-effort fallback
        repository.store(telemetry) ?: return exporter.export(telemetry)
        return Success
    }

    override suspend fun forceFlush(): OperationResultCode = Success

    override suspend fun shutdown(): OperationResultCode = Success
}
