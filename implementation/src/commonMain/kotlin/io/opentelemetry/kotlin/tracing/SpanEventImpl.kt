package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.LazyAttributesHolder
import io.opentelemetry.kotlin.tracing.model.SpanEvent

internal class SpanEventImpl(
    override val name: String,
    override val timestamp: Long,
    attributeLimit: Int,
    attributeValueLengthLimit: Int,
) : LazyAttributesHolder(attributeLimit, attributeValueLengthLimit), SpanEvent
