package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior

/**
 * Captures the simple span processor configured programmatically and maps it onto a behavior.
 */
@ExperimentalApi
class SimpleSpanProcessorConfigDslImpl : BehaviorSupplier<SimpleSpanProcessorBehavior> {
    private var exporter: SpanExporterBehavior? = null

    fun exporter(action: SpanExporterConfigDslImpl.() -> Unit) {
        val impl = SpanExporterConfigDslImpl()
        impl.action()
        exporter = impl.toBehavior()
    }

    override fun toBehavior(): SimpleSpanProcessorBehavior {
        return SimpleSpanProcessorBehavior(exporter = exporter)
    }
}
