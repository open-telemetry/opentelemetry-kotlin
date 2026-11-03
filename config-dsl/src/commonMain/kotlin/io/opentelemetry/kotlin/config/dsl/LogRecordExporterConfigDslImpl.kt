package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior

/**
 * Captures the Log Record exporter configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
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
