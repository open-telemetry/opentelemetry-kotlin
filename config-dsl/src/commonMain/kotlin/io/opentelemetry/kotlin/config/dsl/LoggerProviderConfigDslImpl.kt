package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor

/**
 * Captures logger provider configuration and maps it onto a behavior.
 */
@ExperimentalApi
class LoggerProviderConfigDslImpl : BehaviorSupplier<LoggerProviderBehavior> {

    private var exporter: LogExporterBehavior? = null

    @Suppress("UnusedParameter")
    fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        exporter = LogExporterBehavior.Console
    }

    override fun toBehavior(): LoggerProviderBehavior =
        LoggerProviderBehavior(
            processor = exporter?.let { LogRecordProcessorBehavior(exporter = it) },
        )
}
