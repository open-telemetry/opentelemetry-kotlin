package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A view of [LogRecordModel] that is returned when read and write operations are permissible on a log record.
 */
@OptIn(ExperimentalApi::class)
internal class ReadWriteLogRecordImpl(
    private val impl: LogRecordModel
) : ReadWriteLogRecord by impl
