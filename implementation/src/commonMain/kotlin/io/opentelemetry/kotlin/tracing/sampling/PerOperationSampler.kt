package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

internal class PerOperationSampler(
    private val defaultSampler: Sampler,
    private val byOperation: Map<String, Sampler>,
) : Sampler {

    override val description: String =
        "PerOperationSampler{default:${defaultSampler.description},operationCount:${byOperation.size}}"

    override fun shouldSample(
        context: Context,
        traceIdBytes: ByteArray,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult =
        (byOperation[name] ?: defaultSampler).shouldSample(
            context = context,
            traceIdBytes = traceIdBytes,
            name = name,
            spanKind = spanKind,
            attributes = attributes,
            links = links,
        )
}