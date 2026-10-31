package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior

/**
 * Captures the simple log record processor configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
class SimpleLogRecordProcessorConfigDslImpl : BehaviorSupplier<SimpleLogRecordProcessorBehavior> {
    private var exporter: LogRecordExporterBehavior? = null

    fun exporter(action: LogRecordExporterConfigDslImpl.() -> Unit) {
        val impl = LogRecordExporterConfigDslImpl()
        impl.action()
        exporter = impl.toBehavior()
    }

    override fun toBehavior(): SimpleLogRecordProcessorBehavior {
        return SimpleLogRecordProcessorBehavior(exporter = exporter)
    }
}
