package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior

/**
 * Captures the log record processor configured programmatically, and maps it onto a behavior.
 */
@ExperimentalApi
class LogRecordProcessorConfigDslImpl {

    private var simple: SimpleLogRecordProcessorBehavior? = null
    private var batch: BatchLogRecordProcessorBehavior? = null

    fun toBehavior(): LogRecordProcessorBehavior? = when {
        simple == null && batch == null -> null
        else -> LogRecordProcessorBehavior(simple = simple, batch = batch)
    }

    fun simple(): SimpleLogRecordProcessorBehavior = SimpleLogRecordProcessorBehavior().also {
        simple = it
        batch = null
    }

    fun batch(): BatchLogRecordProcessorBehavior = BatchLogRecordProcessorBehavior().also {
        batch = it
        simple = null
    }
}
