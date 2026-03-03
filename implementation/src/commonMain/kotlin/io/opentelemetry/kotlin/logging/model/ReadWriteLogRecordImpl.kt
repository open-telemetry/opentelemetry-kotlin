package io.opentelemetry.kotlin.logging.model
/**
 * A view of [LogRecordModel] that is returned when read and write operations are permissible on a log record.
 */
internal class ReadWriteLogRecordImpl(
    private val impl: LogRecordModel
) : ReadWriteLogRecord by impl
