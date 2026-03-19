package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingDecision
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.model.TraceState
import io.opentelemetry.kotlin.tracing.model.TraceStateAdapter

internal class SamplerAdapter(
    internal val impl: OtelJavaSampler,
) : Sampler {

    override val description: String = impl.description

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val result = impl.shouldSample(
            context.toOtelJavaContext(),
            traceId,
            name,
            spanKind.toOtelJavaSpanKind(),
            (attributes as? CompatAttributesModel)?.otelJavaAttributes()
                ?: OtelJavaAttributes.empty(),
            emptyList(),
        )
        val decision = when (result.decision) {
            OtelJavaSamplingDecision.DROP -> SamplingResult.Decision.DROP
            OtelJavaSamplingDecision.RECORD_ONLY -> SamplingResult.Decision.RECORD_ONLY
            else -> SamplingResult.Decision.RECORD_AND_SAMPLE
        }
        return object : SamplingResult {
            override val decision = decision
            override val attributes = CompatAttributesModel(result.attributes.toBuilder())
            override val traceState: TraceState = TraceStateAdapter(
                result.getUpdatedTraceState(OtelJavaTraceState.getDefault()),
            )
        }
    }
}
