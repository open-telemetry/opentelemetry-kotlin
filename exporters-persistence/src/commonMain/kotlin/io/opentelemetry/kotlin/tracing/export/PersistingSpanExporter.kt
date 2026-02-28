package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.PersistingExporter
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.TelemetryRepository
import io.opentelemetry.kotlin.tracing.data.SpanData

@ExperimentalApi
internal class PersistingSpanExporter(
    private val exporter: SpanExporter,
    repository: TelemetryRepository<SpanData>,
    private val persistingExporter: PersistingExporter<SpanData> = PersistingExporter(
        exporter::export,
        exporter,
        repository
    )
) : SpanExporter, TelemetryCloseable by persistingExporter {

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode =
        persistingExporter.export(telemetry)
}
