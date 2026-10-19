package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor

/**
 * Maps`tracer_provider.processors` onto processor behavior.
 */
@ExperimentalApi
fun List<SpanProcessor>.toBehavior(): SpanProcessorBehavior? {
    // TODO: Add support to multiple exporters being in use simultaneously once we have the SpanProcessor
    //  fully implemented.
    for ((batch, simple) in this) {
        when {
            batch != null -> return SpanProcessorBehavior(
                batch = batch.toBehavior()
            )
            simple != null -> return SpanProcessorBehavior(
                simple = simple.toBehavior()
            )
        }
    }

    return null
}
