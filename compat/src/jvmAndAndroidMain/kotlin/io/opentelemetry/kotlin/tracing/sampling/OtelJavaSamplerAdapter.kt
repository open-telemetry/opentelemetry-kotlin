package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingDecision
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingResult
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanLink
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
        val links = parentLinks.map { it.toOtelKotlinSpanLink() }
        val result = userCode { delegate.shouldSample(ctx, traceId.hexToByteArray(), name, kind, attrs, links) }

        val decision = when (userCode { result.decision }) {
            DROP -> OtelJavaSamplingDecision.DROP
            RECORD_ONLY -> OtelJavaSamplingDecision.RECORD_ONLY
            RECORD_AND_SAMPLE -> OtelJavaSamplingDecision.RECORD_AND_SAMPLE
        }
        val javaAttributes = attrsFromMap(userCode { result.attributes.attributes })
        val javaTraceState = userCode { result.traceState }.toOtelJavaTraceState()
        return object : OtelJavaSamplingResult {
            override fun getDecision(): OtelJavaSamplingDecision = decision
            override fun getAttributes(): OtelJavaAttributes = javaAttributes
            override fun getUpdatedTraceState(parentTraceState: OtelJavaTraceState): OtelJavaTraceState =
                javaTraceState
        }
    }

    override fun getDescription(): String = delegate.description
}
