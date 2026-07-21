package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * A [ComposableSampler] that never samples, regardless of parent trace state.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwaysoff
 */
public class ComposableAlwaysOffSampler : ComposableSampler {

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent = SamplingIntentImpl(
        threshold = null,
        adjustedCountReliable = false
    )

    override val description: String
        get() = "ComposableAlwaysOffSampler"
}
