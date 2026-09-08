package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior

@ExperimentalApi
class SamplerConfigDslImpl {

    private var chosen: SamplerBehavior? = null

    fun toBehavior(): SamplerBehavior? = chosen

    fun alwaysOn(): SamplerBehavior = record(SamplerBehavior.AlwaysOn)

    fun alwaysOff(): SamplerBehavior = record(SamplerBehavior.AlwaysOff)

    fun parentBased(
        root: SamplerBehavior? = null,
        remoteParentSampled: SamplerBehavior? = null,
        remoteParentNotSampled: SamplerBehavior? = null,
        localParentSampled: SamplerBehavior? = null,
        localParentNotSampled: SamplerBehavior? = null,
    ): SamplerBehavior = record(
        SamplerBehavior.ParentBased(
            root = root,
            remoteParentSampled = remoteParentSampled,
            remoteParentNotSampled = remoteParentNotSampled,
            localParentSampled = localParentSampled,
            localParentNotSampled = localParentNotSampled
        )
    )

    private fun record(value: SamplerBehavior): SamplerBehavior {
        chosen = value
        return value
    }
}