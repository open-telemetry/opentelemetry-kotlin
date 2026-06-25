package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanLinkImpl

internal fun buildSpanLink(
    spanContext: SpanContext,
    attributes: (AttributesMutator.() -> Unit)?,
    spanLimitConfig: SpanLimitConfig
): SpanLink {
    val container = AttributesModel(
        attributeLimit = spanLimitConfig.attributeCountPerLinkLimit,
        attributeValueLengthLimit = spanLimitConfig.attributeValueLengthLimit
    )
    if (attributes != null) {
        attributes(container)
    }
    return SpanLinkImpl(spanContext, container)
}
