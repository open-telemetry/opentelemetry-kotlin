package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.dsl.BehaviorSupplier

/**
 * Captures the simple log record processor configured programmatically and maps it onto a behavior.
 */
@OptIn(ExperimentalApi::class)
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
