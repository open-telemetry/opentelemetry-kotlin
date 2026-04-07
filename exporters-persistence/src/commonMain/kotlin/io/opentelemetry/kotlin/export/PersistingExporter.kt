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

    private val shutdownState: MutableShutdownState = MutableShutdownState()

    suspend fun export(telemetry: List<T>): OperationResultCode =
        shutdownState.ifActive {
            val record = repository.store(telemetry)

            val result = delegateExport(telemetry)
            if (result == Success && record != null) {
                repository.delete(record)
            }
            result
        }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(action = closeable::shutdown)

    override suspend fun forceFlush(): OperationResultCode = closeable.forceFlush()
}
