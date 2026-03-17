package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingDecision
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingResult
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanKind
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision.DROP
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision.RECORD_AND_SAMPLE
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision.RECORD_ONLY

internal class OtelJavaSamplerAdapter(private val delegate: Sampler) : OtelJavaSampler {

    override fun shouldSample(
        parentContext: OtelJavaContext,
        traceId: String,
        name: String,
        spanKind: OtelJavaSpanKind,
        attributes: OtelJavaAttributes,
        parentLinks: List<OtelJavaLinkData>,
    ): OtelJavaSamplingResult {
        val ctx = parentContext.toOtelKotlinContext()
        val kind = spanKind.toOtelKotlinSpanKind()
        val attrs = CompatAttributesModel(attributes.toBuilder())
        val result = delegate.shouldSample(ctx, traceId, name, kind, attrs, emptyList())

        val decision = when (result.decision) {
            DROP -> OtelJavaSamplingDecision.DROP
            RECORD_ONLY -> OtelJavaSamplingDecision.RECORD_ONLY
            RECORD_AND_SAMPLE -> OtelJavaSamplingDecision.RECORD_AND_SAMPLE
        }
        return OtelJavaSamplingResult.create(decision, OtelJavaAttributes.empty())
    }

    override fun getDescription(): String = delegate.description
}
