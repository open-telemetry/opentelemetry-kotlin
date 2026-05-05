package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

internal class ParentBasedSampler(
    private val root: Sampler,
    private val remoteParentSampled: Sampler,
    private val remoteParentNotSampled: Sampler,
    private val localParentSampled: Sampler,
    private val localParentNotSampled: Sampler,
) : Sampler {

    override val description: String =
        "ParentBased{" +
            "root:${root.description}," +
            "remoteParentSampled:${remoteParentSampled.description}," +
            "remoteParentNotSampled:${remoteParentNotSampled.description}," +
            "localParentSampled:${localParentSampled.description}," +
            "localParentNotSampled:${localParentNotSampled.description}" +
            "}"

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        val parent = context.extractSpan().spanContext
        val delegate = when {
            !parent.isValid -> root
            parent.isRemote && parent.traceFlags.isSampled -> remoteParentSampled
            parent.isRemote && !parent.traceFlags.isSampled -> remoteParentNotSampled
            parent.traceFlags.isSampled -> localParentSampled
            else -> localParentNotSampled
        }
        return delegate.shouldSample(context, traceId, name, spanKind, attributes, links)
    }
}
