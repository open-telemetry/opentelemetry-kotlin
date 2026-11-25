package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.tracing.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor

/**
 * Maps`tracer_provider.processors` onto processor behavior.
 */
@OptIn(ExperimentalApi::class)
fun List<SpanProcessor>.toBehavior(): List<SpanProcessorBehavior> {
    val processors = mutableListOf<SpanProcessorBehavior>()
    for ((batch, simple) in this) {
        when {
            batch != null -> processors += SpanProcessorBehavior(
                batch = batch.toBehavior()
            )
            simple != null -> processors += SpanProcessorBehavior(
                simple = simple.toBehavior()
            )
        }
    }
    return processors
}
