package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanCreationAction

internal class SpanCreationCollector(
    private val spanLimitConfig: SpanLimitConfig,
    private val attrs: AttributesModel = AttributesModel(
        attributeLimit = spanLimitConfig.attributeCountLimit,
        attributeValueLengthLimit = spanLimitConfig.attributeValueLengthLimit
    )
) : SpanCreationAction, AttributesMutator by attrs {
    private val linksList = mutableListOf<SpanLink>()
    val attributes: AttributeContainer get() = attrs
    val links: List<SpanLink> get() = linksList.toList()

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?,
    ) {
        if (linksList.size < spanLimitConfig.linkCountLimit && !hasSpanContext(spanContext)) {
            linksList.add(buildSpanLink(spanContext, attributes, spanLimitConfig))
        }
    }

    private fun hasSpanContext(spanContext: SpanContext): Boolean =
        linksList.any {
            it.spanContext.traceId == spanContext.traceId && it.spanContext.spanId == spanContext.spanId
        }
}
