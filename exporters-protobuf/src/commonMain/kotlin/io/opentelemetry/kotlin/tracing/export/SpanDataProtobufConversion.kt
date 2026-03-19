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
    start_time_unix_nano = startTimestamp,
    end_time_unix_nano = endTimestamp ?: 0,
    attributes = attributes.createKeyValues(),
    status = Status(
        message = status.description ?: "",
        code = Status.StatusCode.fromValue(status.statusCode.ordinal)
            ?: Status.StatusCode.STATUS_CODE_UNSET
    ),
    events = events.toSpanEvent(),
    links = links.toSpanLink()
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
    spanKind = SpanKind.INTERNAL,
    startTimestamp = start_time_unix_nano,
    endTimestamp = end_time_unix_nano,
    resource = resource,
    instrumentationScopeInfo = instrumentationScopeInfo,
    attributes = attributes.toAttributeMap(),
    events = events.map { it.toEventData() },
    links = links.map { it.toLinkData() },
    hasEnded = true
)

private fun List<SpanEventData>.toSpanEvent(): List<Span.Event> = map { it.toSpanEvent() }

private fun SpanEventData.toSpanEvent(): Span.Event = Span.Event(
    name = name,
    time_unix_nano = timestamp,
    attributes = attributes.createKeyValues()
)

private fun List<SpanLinkData>.toSpanLink() = map { it.toLinkData() }

private fun SpanLinkData.toLinkData() = Span.Link(
    trace_id = spanContext.traceIdBytes.toByteString(),
    span_id = spanContext.spanIdBytes.toByteString(),
    attributes = attributes.createKeyValues()
)

private fun Status.toStatusData(): StatusData = when (code) {
    Status.StatusCode.STATUS_CODE_OK -> StatusData.Ok
    Status.StatusCode.STATUS_CODE_ERROR -> StatusData.Error(message.ifEmpty { null })
    else -> StatusData.Unset
}

private fun Span.Event.toEventData(): SpanEventData = DeserializedSpanEventData(
    name = name,
    timestamp = time_unix_nano,
    attributes = attributes.toAttributeMap()
)

private fun Span.Link.toLinkData(): SpanLinkData = DeserializedSpanLinkData(
    spanContext = DeserializedSpanContext(
        traceIdBytes = trace_id.toByteArray(),
        spanIdBytes = span_id.toByteArray()
    ),
    attributes = attributes.toAttributeMap()
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
    override val links: List<SpanLinkData>,
    override val hasEnded: Boolean
) : SpanData

private class DeserializedSpanEventData(
    override val name: String,
    override val timestamp: Long,
    override val attributes: Map<String, Any>
) : SpanEventData

private class DeserializedSpanLinkData(
    override val spanContext: SpanContext,
    override val attributes: Map<String, Any>
) : SpanLinkData
