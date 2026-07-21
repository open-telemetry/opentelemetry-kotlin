package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * ComposableSampler is a specialized interface that is used by the CompositeSampler.
 * It introduces a composable approach to sampling by defining a new method called GetSamplingIntent,
 * which allows multiple samplers to work together in making a sampling decision.
 */
@ExperimentalApi
public interface ComposableSampler {

    /**
     * Returns a [SamplingIntent] structure that indicates the sampler’s preference for sampling a Span,
     * without actually making the final decision.
     */
    public fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingIntent

    /**
     * Returns the sampler name or short description with the configuration. This may be displayed on debug pages or in the logs.
     */
    public val description: String
}

@ExperimentalApi
public interface SamplingIntent {
    /** Rejection threshold in [0, 2^56). `null` means DROP. Lower ⇒ more likely to sample. */
    public val threshold: Long?

    /** Whether the threshold is reliable for span-to-metrics estimation. */
    public val adjustedCountReliable: Boolean

    /** Optional provider of attributes added when the span is sampled. */
    public val attributesProvider: (() -> AttributeContainer)?

    /** Optional provider of a modified TraceState, given the parent state + final decision. */
    public val traceStateProvider: ((TraceState, SamplingResult.Decision) -> TraceState)?
}
