package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.tracing.sampling.ComposableRuleBasedSampler
import io.opentelemetry.kotlin.tracing.sampling.ComposableSampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingPredicate
import io.opentelemetry.kotlin.tracing.sampling.SamplingRule

internal class ComposableRuleBasedConfigImpl(
    private val dsl: SamplerConfigDsl,
) : ComposableRuleBasedConfigDsl, SamplerConfigDsl by dsl {

    private val rules = mutableListOf<SamplingRule>()

    override fun rule(predicate: SamplingPredicate, sampler: SamplerConfigDsl.() -> ComposableSampler) {
        rules.add(SamplingRule(predicate, dsl.sampler()))
    }

    internal fun buildSampler(): ComposableSampler = ComposableRuleBasedSampler(rules.toList())
}
