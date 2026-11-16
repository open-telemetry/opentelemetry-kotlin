package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.LazyAttributesHolder
import io.opentelemetry.kotlin.tracing.model.SpanLink

internal class SpanLinkImpl(
    override val spanContext: SpanContext,
    attributeLimit: Int,
    attributeValueLengthLimit: Int,
) : LazyAttributesHolder(attributeLimit, attributeValueLengthLimit), SpanLink
