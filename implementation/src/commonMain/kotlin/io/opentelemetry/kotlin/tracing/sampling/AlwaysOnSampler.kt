package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.EmptyAttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision

internal object AlwaysOnSampler : Sampler {

    override val description: String = "AlwaysOnSampler"

    override fun shouldSample(
        context: Context,
        traceIdBytes: ByteArray,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val parentTraceState = context.extractSpan().spanContext.traceState
        return SamplingResultImpl(
            decision = Decision.RECORD_AND_SAMPLE,
            attributes = EmptyAttributeContainer,
            traceState = parentTraceState,
        )
    }
}
