package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.conversion.createKeyValues
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.proto.trace.v1.Span
import io.opentelemetry.proto.trace.v1.Status
import okio.ByteString.Companion.toByteString

@OptIn(ExperimentalApi::class)
fun SpanData.toProtobuf() = Span(
    name = name,
    trace_id = spanContext.traceIdBytes.toByteString(),
    span_id = spanContext.spanIdBytes.toByteString(),
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
