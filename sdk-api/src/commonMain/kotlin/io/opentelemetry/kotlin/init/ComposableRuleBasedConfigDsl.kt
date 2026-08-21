package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.sampling.ComposableSampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingPredicate

/**
 * Configures the ordered list of rules that are evaluated by a rule-based [ComposableSampler].
 *
 * Rules are evaluated in the order they are declared and the first matching rule decides how the
 * span is sampled. If no rule matches, the span is not sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablerulebased
 */
@ExperimentalApi
@ConfigDsl
public interface ComposableRuleBasedConfigDsl : SamplerConfigDsl {

    /**
     * Adds a rule that delegates to the [ComposableSampler] returned by [sampler] when [predicate]
     * matches. Rules that were declared earlier take precedence.
     */
    public fun rule(predicate: SamplingPredicate, sampler: SamplerConfigDsl.() -> ComposableSampler)
}
