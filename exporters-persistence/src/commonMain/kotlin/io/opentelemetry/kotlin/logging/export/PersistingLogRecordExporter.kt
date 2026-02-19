package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.export.TelemetryRepository
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@ExperimentalApi
internal class PersistingLogRecordExporter(
    exporters: List<LogRecordExporter>,
    private val repository: TelemetryRepository<ReadableLogRecord>,
) : LogRecordExporter {

    @Suppress("DEPRECATION")
    private val exporter = createCompositeLogRecordExporter(exporters)

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        val record = repository.store(telemetry)

        val result = exporter.export(telemetry)
        if (result == Success && record != null) {
            repository.delete(record)
        }
        return result
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
