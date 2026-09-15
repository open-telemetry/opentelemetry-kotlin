package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanBuilder
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanCreationAction
import io.opentelemetry.kotlin.tracing.SpanLinkCompatImpl
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext

/**
 * Collects the attributes and links supplied in the span creation lambda so that they can be
 * applied to an [OtelJavaSpanBuilder] before the span is started. This is required for sampling.
 */
@OptIn(ExperimentalApi::class)
internal class CompatSpanCreationCollector(
    private val spanLimitsConfig: CompatSpanLimitsConfig,
    private val attrs: CompatAttributesModel = CompatAttributesModel(),
) : SpanCreationAction, AttributeContainer, AttributesMutator by attrs {

    private val linksImpl = mutableListOf<SpanLinkCompatImpl>()

    override val attributes: Map<String, Any>
        get() = attrs.attributes.entries
            .take(spanLimitsConfig.effectiveAttributeCountLimit)
            .associate { it.key to it.value }

    val links: List<SpanLink> get() = linksImpl.take(spanLimitsConfig.effectiveLinkCountLimit)

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val container = CompatAttributesModel()
        if (attributes != null) {
            attributes(container)
        }
        linksImpl.add(SpanLinkCompatImpl(spanContext, container))
    }

    fun applyTo(builder: OtelJavaSpanBuilder) {
        builder.setAllAttributes(attrs.otelJavaAttributes())
        linksImpl.forEach {
            builder.addLink(it.spanContext.toOtelJavaSpanContext(), it.attrs.otelJavaAttributes())
        }
    }
}
