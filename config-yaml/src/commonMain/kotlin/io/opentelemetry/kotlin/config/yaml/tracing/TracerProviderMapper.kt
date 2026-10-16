package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.schema.model.TracerProvider

/**
 * Maps the `tracer_provider` section onto its behavior, or null if nothing in it is configured.
 */
@ExperimentalApi
fun TracerProvider.toBehavior(): TracerProviderBehavior? {
    val spanLimits = limits?.toBehavior()
    val processor = processors.toBehavior()
    val sampler = sampler?.toBehavior()
    val idGenerator = idGenerator?.toBehavior()
    if (spanLimits == null && processor == null && sampler == null && idGenerator == null) {
        return null
    }
    return TracerProviderBehavior(
        spanLimits = spanLimits,
        processor = processor,
        sampler = sampler,
        idGenerator = idGenerator,
    )
}
