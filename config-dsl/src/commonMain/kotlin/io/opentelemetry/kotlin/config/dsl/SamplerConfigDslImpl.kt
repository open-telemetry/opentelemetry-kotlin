package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior

/**
 * Captures the sampler configured programmatically, and maps it onto a behavior.
 */
@ExperimentalApi
class SamplerConfigDslImpl {

    private var sampler: SamplerBehavior? = null

    fun toBehavior(): SamplerBehavior? = sampler

    fun alwaysOn(): SamplerBehavior = SamplerBehavior.AlwaysOn.also { sampler = it }

    fun alwaysOff(): SamplerBehavior = SamplerBehavior.AlwaysOff.also { sampler = it }

    fun parentBased(
        root: SamplerBehavior? = null,
        remoteParentSampled: SamplerBehavior? = null,
        remoteParentNotSampled: SamplerBehavior? = null,
        localParentSampled: SamplerBehavior? = null,
        localParentNotSampled: SamplerBehavior? = null,
    ): SamplerBehavior =
        SamplerBehavior.ParentBased(
            root = root,
            remoteParentSampled = remoteParentSampled,
            remoteParentNotSampled = remoteParentNotSampled,
            localParentSampled = localParentSampled,
            localParentNotSampled = localParentNotSampled
        ).also { sampler = it }
}
