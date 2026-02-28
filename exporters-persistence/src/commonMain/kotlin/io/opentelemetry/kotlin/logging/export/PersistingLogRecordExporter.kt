package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.PersistingExporter
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.TelemetryRepository
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@ExperimentalApi
internal class PersistingLogRecordExporter(
    private val exporter: LogRecordExporter,
    repository: TelemetryRepository<ReadableLogRecord>,
    private val persistingExporter: PersistingExporter<ReadableLogRecord> = PersistingExporter(
        exporter::export,
        exporter,
        repository
    )
) : LogRecordExporter, TelemetryCloseable by persistingExporter {

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode =
        persistingExporter.export(telemetry)
}
