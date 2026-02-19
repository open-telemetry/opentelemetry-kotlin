package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.FakeEventData
import io.opentelemetry.kotlin.tracing.data.FakeLinkData
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.SpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
class ExportTraceServiceRequestCreatorTest {

    private val telemetry = FakeSpanData(
        parent = FakeSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceFlags = FakeTraceFlags(isSampled = true),
            traceState = FakeTraceState(mapOf("foo" to "bar"))
        ),
        spanContext = FakeSpanContext(
            traceIdBytes = "00000000901234567890123456789012".hexToByteArray(),
            spanIdBytes = "0000000090123456".hexToByteArray(),
            traceFlags = FakeTraceFlags(),
            traceState = FakeTraceState()
        )
    )

    @Test
    fun testProtobufToByteArray() {
        val original = listOf(telemetry)
        val byteArray = original.toProtobufByteArray()
        val result = byteArray.toSpanDataList()

        assertEquals(1, result.size)
        val expected = original[0]
        val span = result[0]
        assertEquals(expected.name, span.name)
        assertSpanContextMatches(expected.spanContext, span.spanContext)
        assertEquals(expected.startTimestamp, span.startTimestamp)
        assertEquals(expected.endTimestamp, span.endTimestamp)
        assertEquals(expected.status.statusCode, span.status.statusCode)
        assertEquals(expected.attributes, span.attributes)
        assertEquals(expected.resource.attributes, span.resource.attributes)
        assertScopeMatches(expected.instrumentationScopeInfo, span.instrumentationScopeInfo)

        assertEquals(expected.events.size, span.events.size)
        assertEventMatches(expected.events[0], span.events[0])

        assertEquals(expected.links.size, span.links.size)
        assertLinkMatches(expected.links[0], span.links[0])
    }

    @Test
    fun testStatusDataOkRoundTrip() {
        val spanData = FakeSpanData(status = StatusData.Ok)
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()
        assertEquals(StatusData.Ok, result[0].status)
    }

    @Test
    fun testStatusDataErrorRoundTrip() {
        val spanData = FakeSpanData(status = StatusData.Error("error message"))
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()
        assertTrue(result[0].status is StatusData.Error)
        assertEquals("error message", (result[0].status as StatusData.Error).description)
    }

    @Test
    fun testStatusDataErrorWithNullDescriptionRoundTrip() {
        val spanData = FakeSpanData(status = StatusData.Error(null))
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()
        assertTrue(result[0].status is StatusData.Error)
        assertNull((result[0].status as StatusData.Error).description)
    }

    @Test
    fun testStatusDataUnsetRoundTrip() {
        val spanData = FakeSpanData(status = StatusData.Unset)
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()
        assertEquals(StatusData.Unset, result[0].status)
    }

    @Test
    fun testMultipleEventsRoundTrip() {
        val events = listOf(
            FakeEventData(name = "event1", timestamp = 1000L, attributes = mapOf("key1" to "val1")),
            FakeEventData(name = "event2", timestamp = 2000L, attributes = mapOf("key2" to "val2")),
            FakeEventData(name = "event3", timestamp = 3000L, attributes = emptyMap())
        )
        val spanData = FakeSpanData(events = events)
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()

        assertEquals(3, result[0].events.size)
        assertEquals("event1", result[0].events[0].name)
        assertEquals(1000L, result[0].events[0].timestamp)
        assertEquals(mapOf("key1" to "val1"), result[0].events[0].attributes)
        assertEquals("event2", result[0].events[1].name)
        assertEquals("event3", result[0].events[2].name)
        assertEquals(emptyMap(), result[0].events[2].attributes)
    }

    @Test
    fun testMultipleLinksRoundTrip() {
        val links = listOf(
            FakeLinkData(
                spanContext = FakeSpanContext(
                    traceIdBytes = "11111111111111111111111111111111".hexToByteArray(),
                    spanIdBytes = "1111111111111111".hexToByteArray()
                ),
                attributes = mapOf("link1" to "value1")
            ),
            FakeLinkData(
                spanContext = FakeSpanContext(
                    traceIdBytes = "22222222222222222222222222222222".hexToByteArray(),
                    spanIdBytes = "2222222222222222".hexToByteArray()
                ),
                attributes = mapOf("link2" to "value2")
            )
        )
        val spanData = FakeSpanData(links = links)
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()

        assertEquals(2, result[0].links.size)
        assertEquals(mapOf("link1" to "value1"), result[0].links[0].attributes)
        assertEquals(mapOf("link2" to "value2"), result[0].links[1].attributes)
    }

    @Test
    fun testEmptyEventsAndLinksRoundTrip() {
        val spanData = FakeSpanData(events = emptyList(), links = emptyList())
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()

        assertEquals(0, result[0].events.size)
        assertEquals(0, result[0].links.size)
    }

    @Test
    fun testComplexAttributesRoundTrip() {
        val attributes = mapOf(
            "string" to "value",
            "long" to 123L,
            "double" to 45.67,
            "bool" to true,
            "list" to listOf("a", 1L, true)
        )
        val spanData = FakeSpanData(attributes = attributes)
        val byteArray = listOf(spanData).toProtobufByteArray()
        val result = byteArray.toSpanDataList()

        assertEquals(attributes, result[0].attributes)
    }

    private fun assertSpanContextMatches(
        expectedContext: SpanContext,
        actualContext: SpanContext
    ) {
        assertEquals(expectedContext.traceId, actualContext.traceId)
        assertEquals(expectedContext.spanId, actualContext.spanId)
        assertEquals(expectedContext.traceFlags.isSampled, actualContext.traceFlags.isSampled)
        assertEquals(expectedContext.traceFlags.isRandom, actualContext.traceFlags.isRandom)
        assertEquals(expectedContext.traceState.asMap(), actualContext.traceState.asMap())
    }

    private fun assertEventMatches(
        expectedEvent: EventData,
        observedEvent: EventData
    ) {
        assertEquals(expectedEvent.name, observedEvent.name)
        assertEquals(expectedEvent.timestamp, observedEvent.timestamp)
        assertEquals(expectedEvent.attributes, observedEvent.attributes)
    }

    private fun assertLinkMatches(
        expectedLink: LinkData,
        observedLink: LinkData
    ) {
        assertEquals(expectedLink.spanContext.traceId, observedLink.spanContext.traceId)
        assertEquals(expectedLink.spanContext.spanId, observedLink.spanContext.spanId)
        assertEquals(expectedLink.attributes, observedLink.attributes)
    }

    private fun assertScopeMatches(
        expectedScope: InstrumentationScopeInfo,
        actualScope: InstrumentationScopeInfo
    ) {
        assertEquals(expectedScope.name, actualScope.name)
        assertEquals(expectedScope.version, actualScope.version)
        assertEquals(expectedScope.attributes, actualScope.attributes)
    }

    @Test
    fun testCreateExportTraceServiceRequest() {
        val request = listOf(telemetry).toExportTraceServiceRequest()
        assertEquals(1, request.resource_spans.size)
        val resourceSpans = request.resource_spans[0]
        assertEquals(1, resourceSpans.scope_spans.size)

        val scopeSpans = resourceSpans.scope_spans[0]
        assertEquals(1, scopeSpans.spans.size)
        val span = scopeSpans.spans[0]
        assertEquals(telemetry.spanContext.traceId, span.trace_id.toByteArray().toHexString())
    }
}
