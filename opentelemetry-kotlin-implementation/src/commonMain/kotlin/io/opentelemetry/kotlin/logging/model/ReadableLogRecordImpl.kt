package io.opentelemetry.kotlin.logging.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A view of [LogRecordModel] that is returned when only read operations are permissible on a log record.
 */
@OptIn(ExperimentalApi::class)
internal class ReadableLogRecordImpl(
    private val impl: ReadWriteLogRecord
) : ReadableLogRecord by impl
