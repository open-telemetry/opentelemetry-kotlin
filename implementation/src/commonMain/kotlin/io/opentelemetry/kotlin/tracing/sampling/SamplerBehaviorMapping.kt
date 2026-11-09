package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.config.dsl.mapToSampler
import io.opentelemetry.kotlin.init.SamplerConfigDsl

/**
 * Binds [SamplerBehavior.mapToSampler] to this SDK's built-in sampler factories.
 */
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
