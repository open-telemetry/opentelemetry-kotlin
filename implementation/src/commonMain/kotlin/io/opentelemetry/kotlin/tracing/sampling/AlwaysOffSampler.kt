package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision

internal class AlwaysOffSampler : Sampler {

    override val description: String = "AlwaysOffSampler"

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val parentTraceState = context.extractSpan().spanContext.traceState
        return SamplingResultImpl(
            decision = Decision.DROP,
            attributes = AttributesModel(),
            traceState = parentTraceState,
        )
    }
}
