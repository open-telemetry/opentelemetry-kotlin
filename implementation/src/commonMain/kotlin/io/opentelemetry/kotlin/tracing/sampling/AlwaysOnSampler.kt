package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.TraceStateImpl
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwayson
 */
internal object AlwaysOnSampler : Sampler {
    private val result = SamplingResultImpl(
        decision = SamplingResult.Decision.RECORD_AND_SAMPLE,
        attributes = AttributesModel(),
        traceState = TraceStateImpl.create(),
    )

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult = result

    override val description: String = "AlwaysOnSampler"
}
