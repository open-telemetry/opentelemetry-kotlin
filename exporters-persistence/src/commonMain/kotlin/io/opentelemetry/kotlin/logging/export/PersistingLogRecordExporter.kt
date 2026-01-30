package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@ExperimentalApi
internal class PersistingLogRecordExporter(
    exporters: List<LogRecordExporter>,
) : LogRecordExporter {

    private val exporter = createCompositeLogRecordExporter(exporters)

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        // TODO: future: persist telemetry to disk, then attempt regular export.
        return exporter.export(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
    override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
}
