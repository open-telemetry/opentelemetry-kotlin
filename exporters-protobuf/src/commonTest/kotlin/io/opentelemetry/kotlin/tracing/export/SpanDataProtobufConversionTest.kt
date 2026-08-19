package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.export.assertAttributesMatch
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.data.FakeSpanEventData
import io.opentelemetry.kotlin.tracing.data.FakeSpanLinkData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.proto.trace.v1.Span
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpanDataProtobufConversionTest {

    private val remoteContext = FakeSpanContext(
        traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
        spanIdBytes = "1234567890123456".hexToByteArray(),
        isRemote = true,
    )

    private val localContext = FakeSpanContext(
        traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
        spanIdBytes = "6543210987654321".hexToByteArray(),
    )

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
        assertLinksMatch(obj.links, protobuf.links, expectedFlags = 0x01)
        assertEquals(0, protobuf.dropped_links_count)
        assertEquals(0, protobuf.dropped_events_count)
    }

    @Test
    fun testDroppedLinksCount() {
        val links = listOf(FakeSpanLinkData(), FakeSpanLinkData())
        val obj = FakeSpanData(links = links, droppedLinksCount = 3)
        val protobuf = obj.toProtobuf()

        assertEquals(links.size, protobuf.links.size)
        assertEquals(3, protobuf.dropped_links_count)
    }

    @Test
    fun testDroppedEventsCount() {
        val events = listOf(FakeSpanEventData(), FakeSpanEventData())
        val obj = FakeSpanData(events = events, droppedEventsCount = 3)
        val protobuf = obj.toProtobuf()

        assertEquals(events.size, protobuf.events.size)
        assertEquals(3, protobuf.dropped_events_count)
    }

    @Test
    fun testDroppedAttributesCountConversion() {
        val obj = FakeSpanData(
            droppedAttributesCount = 3,
            events = listOf(FakeSpanEventData(droppedAttributesCount = 4)),
            links = listOf(FakeSpanLinkData(droppedAttributesCount = 5)),
        )
        val protobuf = obj.toProtobuf()

        assertEquals(3, protobuf.dropped_attributes_count)
        assertEquals(4, protobuf.events.single().dropped_attributes_count)
        assertEquals(5, protobuf.links.single().dropped_attributes_count)

        val roundTrip = protobuf.toSpanData(FakeResource(), FakeInstrumentationScopeInfo())
        assertEquals(3, roundTrip.droppedAttributesCount)
        assertEquals(4, roundTrip.events.single().droppedAttributesCount)
        assertEquals(5, roundTrip.links.single().droppedAttributesCount)
    }

    @Test
    fun testSpanFlags() {
        val sampledAndRandom = FakeSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceFlags = FakeTraceFlags(isSampled = true, isRandom = true),
        )

        // parent is invalid, so bits 8/9 are left in the 'unknown' state
        assertEquals(0x01, FakeSpanData(parent = FakeSpanContext.INVALID).toProtobuf().flags)

        // parent is valid and local, so only the has_is_remote bit is set
        assertEquals(0x101, FakeSpanData(parent = localContext).toProtobuf().flags)

        // parent is valid and remote, so both bits are set
        assertEquals(0x301, FakeSpanData(parent = remoteContext).toProtobuf().flags)

        // the span's own trace flags occupy bits 0-7 alongside the parent's is_remote bits
        val protobuf = FakeSpanData(
            spanContext = sampledAndRandom,
            parent = remoteContext,
        ).toProtobuf()
        assertEquals(0x303, protobuf.flags)
    }

    @Test
    fun testLinkTraceStateAndFlags() {
        val links = listOf(
            FakeSpanLinkData(spanContext = remoteContext),
            FakeSpanLinkData(spanContext = localContext),
            FakeSpanLinkData(
                spanContext = FakeSpanContext(
                    traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
                    spanIdBytes = "1234567890123456".hexToByteArray(),
                    traceState = FakeTraceState(emptyMap()),
                )
            ),
        )
        val protobuf = FakeSpanData(links = links).toProtobuf()

        assertEquals("foo=bar", protobuf.links[0].trace_state)
        assertEquals(0x301, protobuf.links[0].flags)

        assertEquals("foo=bar", protobuf.links[1].trace_state)
        assertEquals(0x101, protobuf.links[1].flags)

        assertEquals("", protobuf.links[2].trace_state)
        assertEquals(0x101, protobuf.links[2].flags)
    }

    @Test
    fun testTraceStateAndFlagsRoundTrip() {
        val obj = FakeSpanData(
            parent = remoteContext,
            spanContext = localContext,
            links = listOf(FakeSpanLinkData(spanContext = remoteContext)),
        )
        val roundTrip = obj.toProtobuf().toSpanData(FakeResource(), FakeInstrumentationScopeInfo())

        assertTrue(roundTrip.parent.isRemote)
        assertTrue(roundTrip.spanContext.traceFlags.isSampled)
        assertFalse(roundTrip.spanContext.traceFlags.isRandom)
        assertEquals(mapOf("foo" to "bar"), roundTrip.spanContext.traceState.asMap())

        val link = roundTrip.links.single()
        assertTrue(link.spanContext.isRemote)
        assertTrue(link.spanContext.traceFlags.isSampled)
        assertEquals(mapOf("foo" to "bar"), link.spanContext.traceState.asMap())
    }

    @Test
    fun testLocalParentRoundTrip() {
        val obj = FakeSpanData(
            parent = localContext,
            links = listOf(FakeSpanLinkData(spanContext = localContext)),
        )
        val roundTrip = obj.toProtobuf().toSpanData(FakeResource(), FakeInstrumentationScopeInfo())

        assertFalse(roundTrip.parent.isRemote)
        assertFalse(roundTrip.links.single().spanContext.isRemote)
    }

    @Test
    fun testSpanKindMapping() {
        val kindMappings = mapOf(
            SpanKind.INTERNAL to Span.SpanKind.SPAN_KIND_INTERNAL,
            SpanKind.SERVER to Span.SpanKind.SPAN_KIND_SERVER,
            SpanKind.CLIENT to Span.SpanKind.SPAN_KIND_CLIENT,
            SpanKind.PRODUCER to Span.SpanKind.SPAN_KIND_PRODUCER,
            SpanKind.CONSUMER to Span.SpanKind.SPAN_KIND_CONSUMER,
        )
        kindMappings.forEach { (spanKind, protoKind) ->
            val protobuf = FakeSpanData(spanKind = spanKind).toProtobuf()
            assertEquals(protoKind, protobuf.kind)
        }
    }

    private fun assertEventsMatch(
        events: List<SpanEventData>, eventsList: List<Span.Event>
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
        links: List<SpanLinkData>, linksList: List<Span.Link>, expectedFlags: Int
    ) {
        assertEquals(links.size, linksList.size)
        links.forEachIndexed { index, link ->
            val proto = linksList[index]
            assertEquals(link.spanContext.traceId, proto.trace_id.toByteArray().toHexString())
            assertEquals(link.spanContext.spanId, proto.span_id.toByteArray().toHexString())
            assertEquals("foo=bar", proto.trace_state)
            assertEquals(expectedFlags, proto.flags)
            assertAttributesMatch(link.attributes, proto.attributes)
        }
    }
}