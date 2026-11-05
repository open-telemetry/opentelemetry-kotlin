package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData

internal fun SpanEventData.toSnapshot(): SpanEventData = SnapshotSpanEventData(
    name = name,
    timestamp = timestamp,
    attributes = attributes,
    droppedAttributesCount = droppedAttributesCount,
)

internal fun SpanLinkData.toSnapshot(): SpanLinkData = SnapshotSpanLinkData(
    spanContext = spanContext,
    attributes = attributes,
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
