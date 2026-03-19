package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision

internal class AlwaysOffSampler(private val spanFactory: SpanFactory) : Sampler {

    override val description: String = "AlwaysOffSampler"

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val parentTraceState = spanFactory.fromContext(context).spanContext.traceState
        return SamplingResultImpl(
            decision = Decision.DROP,
            attributes = AttributesModel(),
            traceState = parentTraceState,
        )
    }
}
