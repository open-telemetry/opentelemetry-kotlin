package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAlwaysRecordSampler
import io.opentelemetry.kotlin.aliases.OtelJavaComposableSampler
import io.opentelemetry.kotlin.aliases.OtelJavaCompositeSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.ComposableRuleBasedConfigDsl
import io.opentelemetry.kotlin.init.SamplerConfigDsl
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanKind
import io.opentelemetry.sdk.extension.incubator.trace.samplers.SamplingPredicate as OtelJavaSamplingPredicate

/**
 * Provides the built-in samplers backed by opentelemetry-java.
 */
@ExperimentalApi
internal class CompatSamplerConfig(override val spanFactory: SpanFactory) : SamplerConfigDsl {

    override fun alwaysOn(): Sampler = SamplerAdapter(OtelJavaSampler.alwaysOn())

    override fun alwaysOff(): Sampler = SamplerAdapter(OtelJavaSampler.alwaysOff())

    override fun alwaysRecord(root: Sampler): Sampler =
        SamplerAdapter(OtelJavaAlwaysRecordSampler.create(root.toOtelJavaSampler()))

    override fun parentBased(
        root: Sampler,
        remoteParentSampled: Sampler,
        remoteParentNotSampled: Sampler,
        localParentSampled: Sampler,
        localParentNotSampled: Sampler,
    ): Sampler = SamplerAdapter(
        OtelJavaSampler.parentBasedBuilder(root.toOtelJavaSampler())
            .setRemoteParentSampled(remoteParentSampled.toOtelJavaSampler())
            .setRemoteParentNotSampled(remoteParentNotSampled.toOtelJavaSampler())
            .setLocalParentSampled(localParentSampled.toOtelJavaSampler())
            .setLocalParentNotSampled(localParentNotSampled.toOtelJavaSampler())
            .build()
    )

    override fun composite(block: SamplerConfigDsl.() -> ComposableSampler): Sampler =
        SamplerAdapter(OtelJavaCompositeSampler.wrap(block().toOtelJavaComposableSampler()))

    override fun composableAlwaysOn(): ComposableSampler =
        OtelJavaBackedComposableSampler(OtelJavaComposableSampler.alwaysOn())

    override fun composableAlwaysOff(): ComposableSampler =
        OtelJavaBackedComposableSampler(OtelJavaComposableSampler.alwaysOff())

    override fun composableProbability(ratio: Double): ComposableSampler {
        // opentelemetry-java does not reject out-of-range ratios, but silently drops spans.
        // fail here so we can fallback to the default sampler.
        require(ratio in 0.0..1.0) { "ratio must be between 0 and 1, got $ratio" }
        return OtelJavaBackedComposableSampler(OtelJavaComposableSampler.probability(ratio))
    }

    override fun composableParentThreshold(root: ComposableSampler): ComposableSampler =
        OtelJavaBackedComposableSampler(OtelJavaComposableSampler.parentThreshold(root.toOtelJavaComposableSampler()))

    override fun composableAnnotating(
        delegate: ComposableSampler,
        attributes: AttributesMutator.() -> Unit,
    ): ComposableSampler = OtelJavaBackedComposableSampler(
        OtelJavaComposableSampler.annotating(
            delegate.toOtelJavaComposableSampler(),
            CompatAttributesModel().apply(attributes).otelJavaAttributes(),
        )
    )

    override fun composableRuleBased(block: ComposableRuleBasedConfigDsl.() -> Unit): ComposableSampler =
        OtelJavaBackedComposableSampler(CompatComposableRuleBasedConfig(this).apply(block).build())
}

@ExperimentalApi
private class CompatComposableRuleBasedConfig(
    private val dsl: SamplerConfigDsl,
) : ComposableRuleBasedConfigDsl, SamplerConfigDsl by dsl {

    private val builder = OtelJavaComposableSampler.ruleBasedBuilder()

    override fun rule(predicate: SamplingPredicate, sampler: SamplerConfigDsl.() -> ComposableSampler) {
        builder.add(
            predicate.toOtelJavaSamplingPredicate(),
            dsl.sampler().toOtelJavaComposableSampler(),
        )
    }

    fun build(): OtelJavaComposableSampler = builder.build()
}

private fun SamplingPredicate.toOtelJavaSamplingPredicate(): OtelJavaSamplingPredicate =
    OtelJavaSamplingPredicate { parentContext, _, name, spanKind, attributes, _ ->
        matches(
            parentContext.toOtelKotlinContext(),
            name,
            spanKind.toOtelKotlinSpanKind(),
            CompatAttributesModel(attributes.toBuilder()),
            emptyList(),
        )
    }

private fun Sampler.toOtelJavaSampler(): OtelJavaSampler = when (this) {
    is SamplerAdapter -> impl
    else -> OtelJavaSamplerAdapter(this)
}

private fun ComposableSampler.toOtelJavaComposableSampler(): OtelJavaComposableSampler = when (this) {
    is OtelJavaBackedComposableSampler -> impl
    else -> KotlinComposableSamplerAdapter(this)
}
