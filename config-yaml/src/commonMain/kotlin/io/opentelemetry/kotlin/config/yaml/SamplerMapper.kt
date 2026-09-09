package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.schema.model.ParentBasedSampler
import io.opentelemetry.kotlin.config.schema.model.Sampler

/**
 * Maps the `tracer_provider.sampler` section of a declarative config file onto the behavior it
 * supplies. Anything the file omits, or sets to a value the spec disallows, is left unset.
 *
 * If multiple conflicting samplers are configured, this evaluates to unset (`null`).
 */
@ExperimentalApi
fun Sampler.toBehavior(): SamplerBehavior? {
    val mapped = listOfNotNull(
        alwaysOn?.let { SamplerBehavior.AlwaysOn },
        alwaysOff?.let { SamplerBehavior.AlwaysOff },
        parentBased?.toBehavior()
    )

    return mapped.singleOrNull()
}

private fun ParentBasedSampler.toBehavior(): SamplerBehavior.ParentBased =
    SamplerBehavior.ParentBased(
        root = root?.toBehavior(),
        remoteParentSampled = remoteParentSampled?.toBehavior(),
        remoteParentNotSampled = remoteParentNotSampled?.toBehavior(),
        localParentSampled = localParentSampled?.toBehavior(),
        localParentNotSampled = localParentNotSampled?.toBehavior()
    )
