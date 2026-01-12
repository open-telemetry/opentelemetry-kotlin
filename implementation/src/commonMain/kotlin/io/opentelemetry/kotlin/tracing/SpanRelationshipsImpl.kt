package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainerImpl
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.threadSafeList
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanRelationships

@OptIn(ExperimentalApi::class)
internal class SpanRelationshipsImpl(
    val clock: Clock,
    val spanLimitConfig: SpanLimitConfig,
    val attrs: MutableAttributeContainer = MutableAttributeContainerImpl(spanLimitConfig.attributeCountLimit),
) : SpanRelationships, MutableAttributeContainer by attrs {

    val links = threadSafeList<LinkData>()
    val events = threadSafeList<EventData>()

    override fun addLink(
        spanContext: SpanContext,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        if (links.size < spanLimitConfig.linkCountLimit) {
            val container = MutableAttributeContainerImpl(spanLimitConfig.attributeCountPerLinkLimit)
            if (attributes != null) {
                attributes(container)
            }
            links.add(LinkImpl(spanContext, container))
        }
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        if (events.size < spanLimitConfig.eventCountLimit) {
            val container = MutableAttributeContainerImpl(spanLimitConfig.attributeCountPerEventLimit)
            if (attributes != null) {
                attributes(container)
            }
            events.add(SpanEventImpl(name, timestamp ?: clock.now(), container))
        }
    }
}
