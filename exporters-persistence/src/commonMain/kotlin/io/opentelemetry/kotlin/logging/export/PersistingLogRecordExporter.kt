package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.export.TelemetryRepository
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@ExperimentalApi
internal class PersistingLogRecordExporter(
    private val exporter: LogRecordExporter,
    private val repository: TelemetryRepository<ReadableLogRecord>,
) : LogRecordExporter {

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        // if persistence failed attempt immediate export as a best-effort fallback
        repository.store(telemetry) ?: return exporter.export(telemetry)
        return Success
    }

    override suspend fun forceFlush(): OperationResultCode = Success

    override suspend fun shutdown(): OperationResultCode = Success
}
