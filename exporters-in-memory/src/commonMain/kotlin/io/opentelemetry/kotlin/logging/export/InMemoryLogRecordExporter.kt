package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.data.LogRecordData

/**
 * A log record exporter that stores telemetry in memory. This is intended for development/testing
 * rather than production use.
 */
@ExperimentalApi
public interface InMemoryLogRecordExporter : LogRecordExporter {
    /**
     * A snapshot of log records that have been exported since the last [reset].
     */
    public val exportedLogRecords: List<LogRecordData>

    /**
     * Clears exported log records, leaving previously returned snapshots unchanged.
     * This does not shut down the exporter or reactivate it after shutdown.
     */
    public fun reset()
}
