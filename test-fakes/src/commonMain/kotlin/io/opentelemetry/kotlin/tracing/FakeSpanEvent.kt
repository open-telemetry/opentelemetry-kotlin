package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.tracing.model.SpanEvent

class FakeSpanEvent private constructor(
    override val name: String,
    override val timestamp: Long,
    override val attributes: MutableMap<String, Any>,
) : SpanEvent, AttributesMutator by FakeAttributesMutator(attributes) {

    constructor(name: String, timestamp: Long) : this(name, timestamp, mutableMapOf())

    override val droppedAttributesCount: Int = 0
}
