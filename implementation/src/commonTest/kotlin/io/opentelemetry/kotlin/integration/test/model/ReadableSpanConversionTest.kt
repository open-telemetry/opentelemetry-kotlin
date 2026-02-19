package io.opentelemetry.kotlin.integration.test.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanContext
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanData
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.data.FakeEventData
import io.opentelemetry.kotlin.tracing.data.FakeLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.hex
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ReadableSpanConversionTest {

    @Test
    fun testConversion() {
        val original = FakeReadWriteSpan(
            name = "name",
            spanKind = SpanKind.PRODUCER,
            startTimestamp = 500,
            endTimestamp = 1000,
            attributes = mapOf("foo" to "bar"),
            links = listOf(
                FakeLinkData(
                    FakeSpanContext.INVALID,
                    mapOf("foo" to "bar")
                )
            ),
            events = listOf(
                FakeEventData(
                    "fake_event",
                    500,
                    mapOf("foo" to "bar")
                )
            ),
            resource = FakeResource(
                mapOf("foo" to "bar"),
                "fake_resource",
            ),
            status = StatusData.Error("whoops")
        )
        val observed = original.toSerializable()
        assertEquals(original.name, observed.name)
        assertEquals(original.spanKind.name, observed.kind)
        assertEquals(original.startTimestamp, observed.startTimestamp)
        assertEquals(original.endTimestamp, observed.endTimestamp)
        assertEquals(original.hasEnded, observed.ended)
        assertEquals(original.status.statusCode.name, observed.statusData.name)
        assertEquals(original.status.description, observed.statusData.description)
        assertResource(original, observed)
        assertInstrumentationScopeInfo(original, observed)
        assertAttributes(original, observed)
        assertEvents(original, observed)
        assertLinks(original, observed)
    }

    private fun assertResource(
        original: FakeReadWriteSpan,
        observed: SerializableSpanData
    ) {
        assertEquals(original.resource.schemaUrl, observed.resource.schemaUrl)
        assertEquals(original.resource.attributes, observed.resource.attributes)
    }

    private fun assertInstrumentationScopeInfo(
        original: FakeReadWriteSpan,
        observed: SerializableSpanData
    ) {
        assertEquals(original.instrumentationScopeInfo.name, observed.instrumentationScopeInfo.name)
        assertEquals(
            original.instrumentationScopeInfo.schemaUrl,
            observed.instrumentationScopeInfo.schemaUrl
        )
        assertEquals(
            original.instrumentationScopeInfo.attributes,
            observed.instrumentationScopeInfo.attributes
        )
    }

    private fun assertAttributes(
        original: FakeReadWriteSpan,
        observed: SerializableSpanData
    ) {
        assertEquals(original.attributes.size, observed.totalAttributeCount)
        assertEquals(original.attributes, observed.attributes)
    }

    private fun assertEvents(
        original: FakeReadWriteSpan,
        observed: SerializableSpanData
    ) {
        assertEquals(original.events.size, observed.totalRecordedEvents)
        val origEvent = original.events.single()
        val obsEvent = observed.events.single()
        assertEquals(origEvent.name, obsEvent.name)
        assertEquals(origEvent.timestamp, obsEvent.timestamp)
        assertEquals(origEvent.attributes, obsEvent.attributes)
    }

    private fun assertLinks(
        original: FakeReadWriteSpan,
        observed: SerializableSpanData
    ) {
        assertEquals(original.links.size, observed.totalRecordedLinks)
        val origLink = original.links.single()
        val obsLink = observed.links.single()
        assertEquals(origLink.attributes, obsLink.attributes)

        val origSpanContext = origLink.spanContext
        val obsSpanContext = obsLink.spanContext
        assertSpanContext(origSpanContext, obsSpanContext)
    }

    private fun assertSpanContext(
        lhs: SpanContext,
        rhs: SerializableSpanContext
    ) {
        assertEquals(lhs.traceId, rhs.traceId)
        assertEquals(lhs.spanId, rhs.spanId)
        assertEquals(lhs.traceFlags.hex, rhs.traceFlags)
        assertEquals(lhs.traceState.asMap(), rhs.traceState)
    }
}
