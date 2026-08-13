package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * A [ComposableSampler] that samples a fixed percentage of traces, based on the consistent
 * probability sampling (R >= T) algorithm.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableprobabilitybased
 */
internal class ComposableProbabilitySampler(private val ratio: Double) : ComposableSampler {

    init {
        validateRatio(ratio)
    }

    private val intent = SamplingIntentImpl(
        threshold = thresholdFromRatio(ratio),
        adjustedCountReliable = true
    )

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent = intent

    override val description: String
        get() = "ComposableProbabilitySampler{$ratio}"
}
