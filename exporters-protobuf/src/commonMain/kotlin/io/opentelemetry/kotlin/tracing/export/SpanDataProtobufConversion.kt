package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.export.conversion.DeserializedSpanContext
import io.opentelemetry.kotlin.export.conversion.createKeyValues
import io.opentelemetry.kotlin.export.conversion.toAttributeMap
import io.opentelemetry.kotlin.export.conversion.toFlagsInt
import io.opentelemetry.kotlin.export.conversion.toW3CString
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.proto.trace.v1.Span
import io.opentelemetry.proto.trace.v1.Status
import okio.ByteString.Companion.toByteString

@OptIn(ExperimentalApi::class)
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

@OptIn(ExperimentalApi::class)
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

@OptIn(ExperimentalApi::class)
private fun List<EventData>.toSpanEvent(): List<Span.Event> = map { it.toSpanEvent() }

@OptIn(ExperimentalApi::class)
private fun EventData.toSpanEvent(): Span.Event = Span.Event(
    name = name,
    time_unix_nano = timestamp,
    attributes = attributes.createKeyValues()
)

@OptIn(ExperimentalApi::class)
private fun List<LinkData>.toSpanLink() = map { it.toLinkData() }

@OptIn(ExperimentalApi::class)
private fun LinkData.toLinkData() = Span.Link(
    trace_id = spanContext.traceIdBytes.toByteString(),
    span_id = spanContext.spanIdBytes.toByteString(),
    attributes = attributes.createKeyValues()
)

@OptIn(ExperimentalApi::class)
private fun Status.toStatusData(): StatusData = when (code) {
    Status.StatusCode.STATUS_CODE_OK -> StatusData.Ok
    Status.StatusCode.STATUS_CODE_ERROR -> StatusData.Error(message.ifEmpty { null })
    else -> StatusData.Unset
}

@OptIn(ExperimentalApi::class)
private fun Span.Event.toEventData(): EventData = DeserializedEventData(
    name = name,
    timestamp = time_unix_nano,
    attributes = attributes.toAttributeMap()
)

@OptIn(ExperimentalApi::class)
private fun Span.Link.toLinkData(): LinkData = DeserializedLinkData(
    spanContext = DeserializedSpanContext(
        traceIdBytes = trace_id.toByteArray(),
        spanIdBytes = span_id.toByteArray()
    ),
    attributes = attributes.toAttributeMap()
)

@OptIn(ExperimentalApi::class)
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
    override val events: List<EventData>,
    override val links: List<LinkData>,
    override val hasEnded: Boolean
) : SpanData

@OptIn(ExperimentalApi::class)
private class DeserializedEventData(
    override val name: String,
    override val timestamp: Long,
    override val attributes: Map<String, Any>
) : EventData

@OptIn(ExperimentalApi::class)
private class DeserializedLinkData(
    override val spanContext: SpanContext,
    override val attributes: Map<String, Any>
) : LinkData
