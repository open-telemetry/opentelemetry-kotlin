package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.behavior.BehaviorSupplier

/**
 * Captures the Log Record exporter configured programmatically and maps it onto a behavior.
 */
@OptIn(ExperimentalApi::class)
class LogRecordExporterConfigDslImpl : BehaviorSupplier<LogRecordExporterBehavior> {
    private var console: ConsoleExporterBehavior? = null
    private var http: OtlpHttpLogsExporterBehavior? = null

    fun http(action: OtlpHttpLogsExporterConfigDslImpl.() -> Unit) {
        val impl = OtlpHttpLogsExporterConfigDslImpl()
        impl.action()
        http = impl.toBehavior()
    }

    fun console() {
        console = ConsoleExporterBehavior()
    }

    override fun toBehavior(): LogRecordExporterBehavior {
        return LogRecordExporterBehavior(console = console, http = http)
    }
}
