package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.dsl.BehaviorSupplier

/**
 * Captures the span processor configured programmatically, and maps it onto a behavior.
 */
@OptIn(ExperimentalApi::class)
class SpanProcessorConfigDslImpl : BehaviorSupplier<SpanProcessorBehavior> {

    private var simple: SimpleSpanProcessorBehavior? = null
    private var batch: BatchSpanProcessorBehavior? = null

    override fun toBehavior(): SpanProcessorBehavior = SpanProcessorBehavior(simple = simple, batch = batch)

    fun simple(action: SimpleSpanProcessorConfigDslImpl.() -> Unit) {
        val impl = SimpleSpanProcessorConfigDslImpl()
        impl.action()
        simple = impl.toBehavior()
        batch = null
    }

    fun batch(action: BatchSpanProcessorConfigDslImpl.() -> Unit) {
        val impl = BatchSpanProcessorConfigDslImpl()
        impl.action()
        batch = impl.toBehavior()
        simple = null
    }
}
