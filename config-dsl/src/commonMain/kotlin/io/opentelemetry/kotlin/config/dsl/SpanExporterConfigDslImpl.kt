package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior

/**
 * Captures the span exporter configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
class SpanExporterConfigDslImpl : BehaviorSupplier<SpanExporterBehavior> {
    private var console: ConsoleExporterBehavior? = null
    private var http: OtlpHttpSpanExporterBehavior? = null

    fun http(action: OtlpHttpSpanExporterConfigDslImpl.() -> Unit) {
        val impl = OtlpHttpSpanExporterConfigDslImpl()
        impl.action()
        http = impl.toBehavior()
    }

    fun console() {
        console = ConsoleExporterBehavior()
    }

    override fun toBehavior(): SpanExporterBehavior {
        return SpanExporterBehavior(console = console, http = http)
    }
}
