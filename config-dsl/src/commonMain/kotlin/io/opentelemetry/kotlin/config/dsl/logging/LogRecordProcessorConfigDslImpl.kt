package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.BehaviorSupplier

/**
 * Captures the log record processor configured programmatically, and maps it onto a behavior.
 */
@OptIn(ExperimentalApi::class)
class LogRecordProcessorConfigDslImpl : BehaviorSupplier<LogRecordProcessorBehavior> {

    private var simple: SimpleLogRecordProcessorBehavior? = null
    private var batch: BatchLogRecordProcessorBehavior? = null

    override fun toBehavior(): LogRecordProcessorBehavior = LogRecordProcessorBehavior(simple = simple, batch = batch)

    fun simple(action: SimpleLogRecordProcessorConfigDslImpl.() -> Unit) {
        val impl = SimpleLogRecordProcessorConfigDslImpl()
        impl.action()
        simple = impl.toBehavior()
        batch = null
    }

    fun batch(action: BatchLogRecordProcessorConfigDslImpl.() -> Unit) {
        val impl = BatchLogRecordProcessorConfigDslImpl()
        impl.action()
        batch = impl.toBehavior()
        simple = null
    }
}
