package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.assertAttributesMatch
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.proto.trace.v1.Span
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
class SpanDataProtobufConversionTest {

    @Test
    fun testConversion() {
        val attrs = mapOf(
            "string" to "value",
            "long" to 5L,
            "double" to 10.0,
            "bool" to true,
            "stringList" to listOf("a", "b"),
            "longList" to listOf(5, 10L),
            "doubleList" to listOf(6.0, 12.0),
            "boolList" to listOf(true, false),
        )
        val obj = FakeSpanData(
            attributes = attrs, status = StatusData.Error("Whoops")
        )
        val protobuf = obj.toProtobuf()

        assertEquals(obj.name, protobuf.name)
        assertEquals(obj.spanContext.traceId, protobuf.trace_id.toByteArray().toHexString())
        assertEquals(obj.spanContext.spanId, protobuf.span_id.toByteArray().toHexString())
        assertEquals(obj.startTimestamp, protobuf.start_time_unix_nano)
        assertEquals(obj.endTimestamp, protobuf.end_time_unix_nano)
        assertEquals(obj.status.statusCode.ordinal, protobuf.status?.code?.ordinal)
        assertEquals(obj.status.description, protobuf.status?.message)
        assertAttributesMatch(obj.attributes, protobuf.attributes)
        assertEventsMatch(obj.events, protobuf.events)
        assertLinksMatch(obj.links, protobuf.links)
    }

    private fun assertEventsMatch(
        events: List<EventData>, eventsList: List<Span.Event>
    ) {
        assertEquals(events.size, eventsList.size)
        events.forEachIndexed { index, event ->
            val proto = eventsList[index]
            assertEquals(event.name, proto.name)
            assertEquals(event.timestamp, proto.time_unix_nano)
            assertAttributesMatch(event.attributes, proto.attributes)
        }
    }

    private fun assertLinksMatch(
        links: List<LinkData>, linksList: List<Span.Link>
    ) {
        assertEquals(links.size, linksList.size)
        links.forEachIndexed { index, link ->
            val proto = linksList[index]
            assertEquals(link.spanContext.traceId, proto.trace_id.toByteArray().toHexString())
            assertEquals(link.spanContext.spanId, proto.span_id.toByteArray().toHexString())
            assertAttributesMatch(link.attributes, proto.attributes)
        }
    }
}