package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanLinkImpl

/**
 * Builds a single [SpanLink] with per-link attribute limits applied.
 *
 * Returns null if the link has an invalid [SpanContext] and both its
 * attributes and trace state are empty, as per: https://opentelemetry.io/docs/specs/otel/trace/api/#link
 *
 * Extracted as a shared helper so that [SpanModel] and [SpanCreationCollector]
 * apply the same limits without duplicating the logic.
 */
internal fun buildSpanLink(
    spanContext: SpanContext,
    attributes: (AttributesMutator.() -> Unit)?,
    spanLimitConfig: SpanLimitConfig
): SpanLink? {
    val container = AttributesModel(
        attributeLimit = spanLimitConfig.attributeCountPerLinkLimit,
        attributeValueLengthLimit = spanLimitConfig.attributeValueLengthLimit
    )
    userCode { attributes?.invoke(container) }
    if (!spanContext.isValid && container.attributes.isEmpty() && spanContext.traceState.asMap().isEmpty()) {
        return null
    }
    return SpanLinkImpl(spanContext, container)
}
