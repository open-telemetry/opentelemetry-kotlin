package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.MutableShutdownState
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
    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode =
        shutdownState.ifActive {
            val record = repository.store(telemetry)
            val result = exporter.export(telemetry)
            if (result == Success && record != null) {
                repository.delete(record)
            }
            result
        }

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            exporter.shutdown()
        }
}
