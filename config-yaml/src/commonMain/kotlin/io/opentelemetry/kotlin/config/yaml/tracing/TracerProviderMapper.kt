package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.schema.model.TracerProvider

/**
 * Maps the `tracer_provider` section onto its behavior, or null if nothing in it is configured.
 */
@ExperimentalApi
fun TracerProvider.toBehavior(): TracerProviderBehavior? {
    val behavior = TracerProviderBehavior(
        spanLimits = limits?.toBehavior(),
        processor = processors.toBehavior(),
        sampler = sampler?.toBehavior(),
        idGenerator = idGenerator?.toBehavior(),
    )
    if (behavior == TracerProviderBehavior()) {
        return null
    }
    return behavior
}
