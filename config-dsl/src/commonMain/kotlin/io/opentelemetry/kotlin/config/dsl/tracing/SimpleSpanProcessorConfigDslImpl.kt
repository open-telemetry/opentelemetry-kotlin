package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.tracing.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanExporterBehavior
import io.opentelemetry.kotlin.config.dsl.BehaviorSupplier

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
