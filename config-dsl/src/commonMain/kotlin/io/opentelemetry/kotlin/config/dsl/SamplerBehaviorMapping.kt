package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior

/**
 * Maps a resolved [SamplerBehavior] onto SDK sampler instances.
 */
@ExperimentalApi
fun <T> SamplerBehavior.mapToSampler(
    alwaysOn: () -> T,
    alwaysOff: () -> T,
    parentBased: (
        root: T,
        remoteParentSampled: T,
        remoteParentNotSampled: T,
        localParentSampled: T,
        localParentNotSampled: T,
    ) -> T,
): T = when (this) {
    SamplerBehavior.AlwaysOn -> alwaysOn()
    SamplerBehavior.AlwaysOff -> alwaysOff()
    is SamplerBehavior.ParentBased -> parentBased(
        root?.mapToSampler(alwaysOn, alwaysOff, parentBased) ?: alwaysOn(),
        remoteParentSampled?.mapToSampler(alwaysOn, alwaysOff, parentBased)
            ?: alwaysOn(),
        remoteParentNotSampled?.mapToSampler(
            alwaysOn,
            alwaysOff,
            parentBased,
        ) ?: alwaysOff(),
        localParentSampled?.mapToSampler(alwaysOn, alwaysOff, parentBased)
            ?: alwaysOn(),
        localParentNotSampled?.mapToSampler(alwaysOn, alwaysOff, parentBased)
            ?: alwaysOff(),
    )
}
