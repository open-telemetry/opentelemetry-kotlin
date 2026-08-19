package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.data.LogRecordData

/**
 * A read-only representation of a log record.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#readablelogrecord
 */
@ExperimentalApi
public interface ReadableLogRecord : LogRecordData {

    /**
     * Return an instance of the log record at the time of invocation. The implementation provided
     * should be immutable.
     */
    public fun toLogRecordData(): LogRecordData
}
