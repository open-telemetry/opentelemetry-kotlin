package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingDecision
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.EmptyAttributeContainer
import io.opentelemetry.kotlin.attributes.toOtelJavaAttributes
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaLinkData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinTraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink

internal class SamplerAdapter(
    internal val impl: OtelJavaSampler,
) : Sampler {

    override val description: String = impl.description

    override fun shouldSample(
        context: Context,
        traceIdBytes: ByteArray,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val javaContext = context.toOtelJavaContext()
        val result = impl.shouldSample(
            javaContext,
            traceIdBytes.toHexString(),
            name,
            spanKind.toOtelJavaSpanKind(),
            attributes.toOtelJavaAttributes(),
            links.map { it.toOtelJavaLinkData() },
        )
        val decision = when (result.decision) {
            OtelJavaSamplingDecision.DROP -> SamplingResult.Decision.DROP
            OtelJavaSamplingDecision.RECORD_ONLY -> SamplingResult.Decision.RECORD_ONLY
            else -> SamplingResult.Decision.RECORD_AND_SAMPLE
        }
        val parentTraceState = OtelJavaSpan.fromContext(javaContext).spanContext.traceState
        val resultTraceState = result.getUpdatedTraceState(parentTraceState).toOtelKotlinTraceState()
        val resultAttributes: AttributeContainer = when {
            result.attributes.isEmpty -> EmptyAttributeContainer
            else -> CompatAttributesModel(result.attributes.toBuilder())
        }
        return object : SamplingResult {
            override val decision = decision
            override val attributes: AttributeContainer = resultAttributes
            override val traceState: TraceState = resultTraceState
        }
    }
}
