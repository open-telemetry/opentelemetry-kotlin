package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

/**
 * Processes logs before they are exported as batches.
 */
@ExperimentalApi
public interface LogRecordProcessor : TelemetryCloseable {

    /**
     * Invoked when a log record is emitted.
     *
     * @param log The log record that has been emitted.
     * @param context The context associated with the log record.
     */
    public fun onEmit(log: ReadWriteLogRecord, context: Context)
}
