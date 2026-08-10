package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * A rule that pairs a [SamplingPredicate] with the [ComposableSampler] that decides how spans
 * matching the predicate are sampled.
 */
internal data class SamplingRule(
    val predicate: SamplingPredicate,
    val sampler: ComposableSampler,
)

/**
 * A [ComposableSampler] that evaluates rules in order and returns the [SamplingIntent] of the first
 * rule whose predicate matches. If no rule matches, a non-sampling intent is returned.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablerulebased
 */
internal class ComposableRuleBasedSampler(rules: List<SamplingRule>) : ComposableSampler {

    private val rules: List<SamplingRule> = rules.toList()
    private val noMatch = ComposableAlwaysOffSampler()

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent {
        val match = rules.firstOrNull {
            it.predicate.matches(context, name, spanKind, attributes, links)
        }
        return (match?.sampler ?: noMatch).getSamplingIntent(context, name, spanKind, attributes, links)
    }

    override val description: String
        get() = "ComposableRuleBasedSampler{rules:[${rules.joinToString(",") { it.sampler.description }}]}"
}
