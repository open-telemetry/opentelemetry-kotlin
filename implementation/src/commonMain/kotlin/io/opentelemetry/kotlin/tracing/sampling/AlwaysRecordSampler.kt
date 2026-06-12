package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision.DROP
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision.RECORD_ONLY

internal class AlwaysRecordSampler(
    private val root: Sampler,
) : Sampler {

    override val description: String = "AlwaysRecordSampler{${root.description}}"

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val delegate = root.shouldSample(context, traceId, name, spanKind, attributes, links)
        if (delegate.decision == DROP) {
            return SamplingResultImpl(
                decision = RECORD_ONLY,
                attributes = delegate.attributes,
                traceState = delegate.traceState,
            )
        }
        return delegate
    }
}
