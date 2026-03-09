package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.threadSafeList
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanRelationships

internal class SpanRelationshipsImpl(
    val clock: Clock,
    val spanLimitConfig: SpanLimitConfig,
    val attrs: AttributesMutator = AttributesModel(spanLimitConfig.attributeCountLimit),
) : SpanRelationships, AttributesMutator by attrs {

    val links = threadSafeList<SpanLinkData>()
    val events = threadSafeList<SpanEventData>()

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        if (links.size < spanLimitConfig.linkCountLimit) {
            val container = AttributesModel(spanLimitConfig.attributeCountPerLinkLimit)
            if (attributes != null) {
                attributes(container)
            }
            links.add(SpanLinkImpl(spanContext, container))
        }
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        if (events.size < spanLimitConfig.eventCountLimit) {
            val container = AttributesModel(spanLimitConfig.attributeCountPerEventLimit)
            if (attributes != null) {
                attributes(container)
            }
            events.add(SpanEventImpl(name, timestamp ?: clock.now(), container))
        }
    }
}
