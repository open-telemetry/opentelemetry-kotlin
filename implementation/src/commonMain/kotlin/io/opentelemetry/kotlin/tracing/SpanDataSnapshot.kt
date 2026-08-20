package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData

internal fun Map<String, Any>.frozenCopy(): Map<String, Any> {
    if (isEmpty()) {
        return emptyMap()
    }
    return mapValues { (_, value) -> freezeAttributeValue(value) }
}

private fun freezeAttributeValue(value: Any): Any = when (value) {
    is ByteArray -> value.copyOf()
    is List<*> -> value.toList()
    else -> value
}

internal fun SpanEventData.toSnapshot(): SpanEventData = SnapshotSpanEventData(
    name = name,
    timestamp = timestamp,
    attributes = attributes.frozenCopy(),
    droppedAttributesCount = droppedAttributesCount,
)

internal fun SpanLinkData.toSnapshot(): SpanLinkData = SnapshotSpanLinkData(
    spanContext = spanContext,
    attributes = attributes.frozenCopy(),
    droppedAttributesCount = droppedAttributesCount,
)

private class SnapshotSpanEventData(
    override val name: String,
    override val timestamp: Long,
    override val attributes: Map<String, Any>,
    override val droppedAttributesCount: Int,
) : SpanEventData

private class SnapshotSpanLinkData(
    override val spanContext: SpanContext,
    override val attributes: Map<String, Any>,
    override val droppedAttributesCount: Int,
) : SpanLinkData
