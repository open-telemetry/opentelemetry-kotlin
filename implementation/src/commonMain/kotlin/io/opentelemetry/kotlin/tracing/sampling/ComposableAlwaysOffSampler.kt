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
internal object ComposableAlwaysOffSampler : ComposableSampler {

    private val intent = SamplingIntentImpl(
        threshold = null,
        adjustedCountReliable = false
    )

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent = intent

    override val description: String
        get() = "ComposableAlwaysOffSampler"
}
