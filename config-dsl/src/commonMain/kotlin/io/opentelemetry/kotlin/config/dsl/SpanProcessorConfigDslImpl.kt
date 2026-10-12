package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior

/**
 * Captures the span processor configured programmatically, and maps it onto a behavior.
 */
@ExperimentalApi
class SpanProcessorConfigDslImpl {

    private var simple: SimpleSpanProcessorBehavior? = null
    private var batch: BatchSpanProcessorBehavior? = null

    fun toBehavior(): SpanProcessorBehavior? = when {
        simple == null && batch == null -> null
        else -> SpanProcessorBehavior(simple = simple, batch = batch)
    }

    fun simple(): SimpleSpanProcessorBehavior = SimpleSpanProcessorBehavior().also { simple = it }

    fun batch(): BatchSpanProcessorBehavior = BatchSpanProcessorBehavior().also { batch = it }
}
