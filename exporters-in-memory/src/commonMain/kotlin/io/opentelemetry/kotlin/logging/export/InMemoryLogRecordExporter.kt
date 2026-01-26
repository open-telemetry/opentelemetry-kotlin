package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

/**
 * A log record exporter that stores telemetry in memory. This is intended for development/testing
 * rather than production use.
 */
@ExperimentalApi
public interface InMemoryLogRecordExporter : LogRecordExporter {
    /**
     * A list of log records that have been exported.
     */
    public val exportedLogRecords: List<ReadableLogRecord>
}
