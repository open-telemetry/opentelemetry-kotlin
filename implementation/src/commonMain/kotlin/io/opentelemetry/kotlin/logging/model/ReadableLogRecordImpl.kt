package io.opentelemetry.kotlin.logging.model
/**
 * A view of [LogRecordModel] that is returned when only read operations are permissible on a log record.
 */
internal class ReadableLogRecordImpl(
    private val impl: ReadWriteLogRecord
) : ReadableLogRecord by impl
