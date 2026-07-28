package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * A [ComposableSampler] that always samples, regardless of parent trace state.
 *
 * The rejection threshold is always `0`, meaning every randomness value satisfies `R >= T`,
 * and the resulting adjusted count is always reliable since the threshold is not data-dependent.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwayson
 */
public class ComposableAlwaysOnSampler : ComposableSampler {

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent = SamplingIntentImpl(
        threshold = 0,
        adjustedCountReliable = true
    )

    override val description: String
        get() = "ComposableAlwaysOnSampler"
}
