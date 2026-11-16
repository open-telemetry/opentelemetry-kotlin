package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOffSampler
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.AlwaysRecordSampler
import io.opentelemetry.kotlin.tracing.sampling.ComposableAlwaysOffSampler
import io.opentelemetry.kotlin.tracing.sampling.ComposableAlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.ComposableAnnotatingSampler
import io.opentelemetry.kotlin.tracing.sampling.ComposableParentThresholdSampler
import io.opentelemetry.kotlin.tracing.sampling.ComposableProbabilitySampler
import io.opentelemetry.kotlin.tracing.sampling.ComposableSampler
import io.opentelemetry.kotlin.tracing.sampling.CompositeSampler
import io.opentelemetry.kotlin.tracing.sampling.MIN_SAMPLING_RATIO
import io.opentelemetry.kotlin.tracing.sampling.ParentBasedSampler
import io.opentelemetry.kotlin.tracing.sampling.ProbabilitySampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler

internal class SamplerConfigImpl(
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) : SamplerConfigDsl {

    override fun alwaysOn(): Sampler = AlwaysOnSampler

    override fun alwaysOff(): Sampler = AlwaysOffSampler

    override fun alwaysRecord(root: Sampler): Sampler = AlwaysRecordSampler(root)

    // A ratio below 2^-56 rounds to "never sample", so it drops instead of failing validation and
    // falling back to the default sampler, which would sample everything.
    override fun probability(ratio: Double): Sampler =
        if (ratio >= 0.0 && ratio < MIN_SAMPLING_RATIO) {
            AlwaysOffSampler
        } else {
            ProbabilitySampler(ratio, sdkErrorHandler)
        }

    override fun parentBased(
        root: Sampler,
        remoteParentSampled: Sampler,
        remoteParentNotSampled: Sampler,
        localParentSampled: Sampler,
        localParentNotSampled: Sampler,
    ): Sampler = ParentBasedSampler(
        root = root,
        remoteParentSampled = remoteParentSampled,
        remoteParentNotSampled = remoteParentNotSampled,
        localParentSampled = localParentSampled,
        localParentNotSampled = localParentNotSampled,
    )

    override fun composite(block: SamplerConfigDsl.() -> ComposableSampler): Sampler =
        CompositeSampler(block(), sdkErrorHandler)

    override fun composableAlwaysOn(): ComposableSampler = ComposableAlwaysOnSampler

    override fun composableAlwaysOff(): ComposableSampler = ComposableAlwaysOffSampler

    override fun composableProbability(ratio: Double): ComposableSampler =
        if (ratio == 0.0) {
            ComposableAlwaysOffSampler
        } else {
            ComposableProbabilitySampler(ratio)
        }

    override fun composableParentThreshold(root: ComposableSampler): ComposableSampler =
        ComposableParentThresholdSampler(root)

    override fun composableAnnotating(
        delegate: ComposableSampler,
        attributes: AttributesMutator.() -> Unit,
    ): ComposableSampler = ComposableAnnotatingSampler(delegate, attributes)

    override fun composableRuleBased(block: ComposableRuleBasedConfigDsl.() -> Unit): ComposableSampler =
        ComposableRuleBasedConfigImpl(this, sdkErrorHandler).apply(block).buildSampler()
}
