package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.tracing.TracerProviderBehavior
import io.opentelemetry.kotlin.config.schema.model.TracerProvider

/**
 * Maps the `tracer_provider` section onto its behavior.
 */
@OptIn(ExperimentalApi::class)
fun TracerProvider.toBehavior(): TracerProviderBehavior {
    val behavior = TracerProviderBehavior(
        spanLimits = limits?.toBehavior(),
        processors = listOfNotNull(processors.toBehavior()),
        sampler = sampler?.toBehavior(),
        idGenerator = idGenerator?.toBehavior(),
    )
    return behavior
}
