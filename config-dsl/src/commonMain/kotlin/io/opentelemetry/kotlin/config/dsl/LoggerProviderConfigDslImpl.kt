package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor

/**
 * Captures logger provider configuration and maps it onto a behavior.
 */
@ExperimentalApi
class LoggerProviderConfigDslImpl : BehaviorSupplier<LoggerProviderBehavior> {

    private var processor: LogRecordProcessorBehavior? = null

    @Suppress("UnusedParameter")
    fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        processor = LogRecordProcessorBehavior()
    }

    override fun toBehavior(): LoggerProviderBehavior =
        LoggerProviderBehavior(processor = processor)
}
