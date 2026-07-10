package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.export.conversion.DeserializedSpanContext
import io.opentelemetry.kotlin.export.conversion.createKeyValues
import io.opentelemetry.kotlin.export.conversion.toAttributeMap
import io.opentelemetry.kotlin.export.conversion.toFlagsInt
import io.opentelemetry.kotlin.export.conversion.toW3CString
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.proto.trace.v1.Span
import io.opentelemetry.proto.trace.v1.Status
import okio.ByteString.Companion.toByteString

fun SpanData.toProtobuf() = Span(
    name = name,
    trace_id = spanContext.traceIdBytes.toByteString(),
    span_id = spanContext.spanIdBytes.toByteString(),
    trace_state = spanContext.traceState.toW3CString(),
    flags = spanContext.traceFlags.toFlagsInt(),
    parent_span_id = parent.spanIdBytes.toByteString(),
    kind = spanKind.toProtoSpanKind(),
    start_time_unix_nano = startTimestamp,
    end_time_unix_nano = endTimestamp ?: 0,
    attributes = attributes.createKeyValues(),
    status = Status(
        message = status.description ?: "",
        code = Status.StatusCode.fromValue(status.statusCode.ordinal)
            ?: Status.StatusCode.STATUS_CODE_UNSET
    ),
    events = events.toSpanEvent(),
    dropped_events_count = droppedEventsCount,
    links = links.toSpanLink(),
    dropped_links_count = droppedLinksCount,
    dropped_attributes_count = droppedAttributesCount
)

internal fun Span.toSpanData(
    resource: Resource,
    instrumentationScopeInfo: InstrumentationScopeInfo
): SpanData = DeserializedSpanData(
    name = name,
    status = status?.toStatusData() ?: StatusData.Unset,
    parent = DeserializedSpanContext(
        traceIdBytes = trace_id.toByteArray(),
        spanIdBytes = parent_span_id.toByteArray(),
    ),
    spanContext = DeserializedSpanContext(
        traceIdBytes = trace_id.toByteArray(),
        spanIdBytes = span_id.toByteArray(),
        flags = flags,
        traceStateString = trace_state,
    ),
    spanKind = kind.toSpanKind(),
    startTimestamp = start_time_unix_nano,
    endTimestamp = end_time_unix_nano,
    resource = resource,
    instrumentationScopeInfo = instrumentationScopeInfo,
    attributes = attributes.toAttributeMap(),
    events = events.map { it.toEventData() },
    droppedEventsCount = dropped_events_count,
    links = links.map { it.toLinkData() },
    droppedLinksCount = dropped_links_count,
    hasEnded = true,
    droppedAttributesCount = dropped_attributes_count
)

private fun List<SpanEventData>.toSpanEvent(): List<Span.Event> = map { it.toSpanEvent() }

private fun SpanEventData.toSpanEvent(): Span.Event = Span.Event(
    name = name,
    time_unix_nano = timestamp,
    attributes = attributes.createKeyValues(),
    dropped_attributes_count = droppedAttributesCount
)

private fun List<SpanLinkData>.toSpanLink() = map { it.toLinkData() }

private fun SpanLinkData.toLinkData() = Span.Link(
    trace_id = spanContext.traceIdBytes.toByteString(),
    span_id = spanContext.spanIdBytes.toByteString(),
    attributes = attributes.createKeyValues(),
    dropped_attributes_count = droppedAttributesCount
)

private fun SpanKind.toProtoSpanKind(): Span.SpanKind = when (this) {
    SpanKind.SERVER -> Span.SpanKind.SPAN_KIND_SERVER
    SpanKind.CLIENT -> Span.SpanKind.SPAN_KIND_CLIENT
    SpanKind.PRODUCER -> Span.SpanKind.SPAN_KIND_PRODUCER
    SpanKind.CONSUMER -> Span.SpanKind.SPAN_KIND_CONSUMER
    SpanKind.INTERNAL -> Span.SpanKind.SPAN_KIND_INTERNAL
}

private fun Span.SpanKind.toSpanKind(): SpanKind = when (this) {
    Span.SpanKind.SPAN_KIND_SERVER -> SpanKind.SERVER
    Span.SpanKind.SPAN_KIND_CLIENT -> SpanKind.CLIENT
    Span.SpanKind.SPAN_KIND_PRODUCER -> SpanKind.PRODUCER
    Span.SpanKind.SPAN_KIND_CONSUMER -> SpanKind.CONSUMER
    else -> SpanKind.INTERNAL
}

private fun Status.toStatusData(): StatusData = when (code) {
    Status.StatusCode.STATUS_CODE_OK -> StatusData.Ok
    Status.StatusCode.STATUS_CODE_ERROR -> StatusData.Error(message.ifEmpty { null })
    else -> StatusData.Unset
}

private fun Span.Event.toEventData(): SpanEventData = DeserializedSpanEventData(
    name = name,
    timestamp = time_unix_nano,
    attributes = attributes.toAttributeMap(),
    droppedAttributesCount = dropped_attributes_count
)

private fun Span.Link.toLinkData(): SpanLinkData = DeserializedSpanLinkData(
    spanContext = DeserializedSpanContext(
        traceIdBytes = trace_id.toByteArray(),
        spanIdBytes = span_id.toByteArray()
    ),
    attributes = attributes.toAttributeMap(),
    droppedAttributesCount = dropped_attributes_count
)

private class DeserializedSpanData(
    override val name: String,
    override val status: StatusData,
    override val parent: SpanContext,
    override val spanContext: SpanContext,
    override val spanKind: SpanKind,
    override val startTimestamp: Long,
    override val endTimestamp: Long?,
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    override val attributes: Map<String, Any>,
    override val events: List<SpanEventData>,
    override val droppedEventsCount: Int,
    override val links: List<SpanLinkData>,
    override val droppedLinksCount: Int,
    override val hasEnded: Boolean,
    override val droppedAttributesCount: Int
) : SpanData

private class DeserializedSpanEventData(
    override val name: String,
    override val timestamp: Long,
    override val attributes: Map<String, Any>,
    override val droppedAttributesCount: Int
) : SpanEventData

private class DeserializedSpanLinkData(
    override val spanContext: SpanContext,
    override val attributes: Map<String, Any>,
    override val droppedAttributesCount: Int
) : SpanLinkData
