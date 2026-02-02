package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber

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

    /**
     * Returns whether a log record should be emitted based on the provided parameters.
     *
     * This method allows processors to indicate via a boolean return value whether they would
     * filter out a log record before it is created, which helps avoid the cost of creating
     * unnecessary log records.
     *
     * @param context The context associated with the log record.
     * @param instrumentationScopeInfo The instrumentation scope associated with the logger.
     * @param severityNumber The severity of the log record (optional)
     * @param eventName The event name of the log record (optional)
     * @return true if a log record should be emitted
     */
    public fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = true
}
