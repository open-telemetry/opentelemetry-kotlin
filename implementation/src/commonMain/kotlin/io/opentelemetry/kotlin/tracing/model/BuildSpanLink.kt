package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanLinkImpl

/**
 * Builds a single [SpanLink] with per-link attribute limits applied.
 *
 * Extracted as a shared helper so that [SpanModel] and [SpanCreationCollector]
 * apply the same limits without duplicating the logic.
 */
internal fun buildSpanLink(
    spanContext: SpanContext,
    attributes: (AttributesMutator.() -> Unit)?,
    spanLimitConfig: SpanLimitConfig
): SpanLink {
    val container = AttributesModel(
        attributeLimit = spanLimitConfig.attributeCountPerLinkLimit,
        attributeValueLengthLimit = spanLimitConfig.attributeValueLengthLimit
    )
    attributes?.invoke(container)
    return SpanLinkImpl(spanContext, container)
}
