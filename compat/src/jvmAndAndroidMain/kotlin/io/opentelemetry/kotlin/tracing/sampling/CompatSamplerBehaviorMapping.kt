package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.dsl.tracing.mapToSampler
import io.opentelemetry.kotlin.init.SamplerConfigDsl

/**
 * Binds [mapToSampler] to this SDK's built-in sampler factories.
 */
@ExperimentalApi
internal fun SamplerConfigDsl.toSampler(behavior: SamplerBehavior): Sampler =
    behavior.mapToSampler(
        alwaysOn = { alwaysOn() },
        alwaysOff = { alwaysOff() },
        probability = { ratio -> probability(ratio) },
        parentBased = { root, remoteParentSampled, remoteParentNotSampled, localParentSampled, localParentNotSampled ->
            parentBased(
                root = root,
                remoteParentSampled = remoteParentSampled,
                remoteParentNotSampled = remoteParentNotSampled,
                localParentSampled = localParentSampled,
                localParentNotSampled = localParentNotSampled,
            )
        },
    )
