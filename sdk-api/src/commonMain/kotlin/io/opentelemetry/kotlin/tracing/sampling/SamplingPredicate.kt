package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * Decides whether a rule of a rule-based [ComposableSampler] applies to a span that is about to be
 * created. Receives the same parameters as [ComposableSampler.getSamplingIntent].
 *
 * Implementations must not modify the supplied parameters: they are considered read-only state.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablerulebased
 */
@ExperimentalApi
public fun interface SamplingPredicate {

    /**
     * Returns true if the rule guarded by this predicate should decide how the span is sampled.
     */
    public fun matches(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): Boolean
}
