package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode.Success

/**
 * Shared persistence logic for exporters. Stores telemetry before exporting and deletes it only
 * when the export succeeds, ensuring no data loss if the process terminates mid-export.
 */
@ExperimentalApi
internal class PersistingExporter<T>(
    private val delegateExport: suspend (List<T>) -> OperationResultCode,
    private val closeable: TelemetryCloseable,
    private val repository: TelemetryRepository<T>,
) : TelemetryCloseable {

    suspend fun export(telemetry: List<T>): OperationResultCode {
        val record = repository.store(telemetry)

        val result = delegateExport(telemetry)
        if (result == Success && record != null) {
            repository.delete(record)
        }
        return result
    }

    override suspend fun shutdown(): OperationResultCode = closeable.shutdown()
    override suspend fun forceFlush(): OperationResultCode = closeable.forceFlush()
}
