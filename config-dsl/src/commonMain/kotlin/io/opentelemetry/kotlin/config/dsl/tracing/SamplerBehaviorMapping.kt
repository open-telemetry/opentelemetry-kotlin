package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior

/**
 * Maps a resolved [SamplerBehavior] onto SDK sampler instances.
 */
@ExperimentalApi
fun <T> SamplerBehavior.mapToSampler(
    alwaysOn: () -> T,
    alwaysOff: () -> T,
    probability: (ratio: Double) -> T,
    parentBased: (
        root: T,
        remoteParentSampled: T,
        remoteParentNotSampled: T,
        localParentSampled: T,
        localParentNotSampled: T,
    ) -> T,
): T {
    fun SamplerBehavior?.mapOr(default: () -> T): T =
        this?.mapToSampler(alwaysOn, alwaysOff, probability, parentBased) ?: default()

    return when (this) {
        SamplerBehavior.AlwaysOn -> alwaysOn()
        SamplerBehavior.AlwaysOff -> alwaysOff()
        is SamplerBehavior.Probability -> probability(ratio)
        is SamplerBehavior.ParentBased -> parentBased(
            root.mapOr(alwaysOn),
            remoteParentSampled.mapOr(alwaysOn),
            remoteParentNotSampled.mapOr(alwaysOff),
            localParentSampled.mapOr(alwaysOn),
            localParentNotSampled.mapOr(alwaysOff),
        )
    }
}
