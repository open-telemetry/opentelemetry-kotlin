package io.opentelemetry.kotlin.framework.serialization

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.hex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SerializableSpanDataTest {

    @Test
    fun testConversion() {
        val fake = FakeSpanData()
        val observed = fake.toSerializable()

        assertEquals(fake.name, observed.name)
        assertEquals(fake.spanKind.name, observed.kind)
        compareSpanContexts(fake.spanContext, observed.spanContext)
        compareSpanContexts(fake.parent, observed.parentSpanContext)
        compareStatusData(fake.status, observed.statusData)
        assertEquals(fake.startTimestamp, observed.startTimestamp)
        compareAttributes(fake.attributes, observed.attributes)
        compareEvents(fake.events, observed.events)
        compareLinks(fake.links, observed.links)
        assertEquals(fake.endTimestamp, observed.endTimestamp)
        assertTrue(fake.hasEnded)
        assertEquals(fake.events.size, observed.totalRecordedEvents)
        assertEquals(fake.links.size, observed.totalRecordedLinks)
        assertEquals(fake.attributes.size, observed.totalAttributeCount)
        compareResource(fake.resource, observed.resource)
    }

    private fun compareSpanContexts(expected: SpanContext, observed: SerializableSpanContext) {
        assertEquals(expected.traceId, observed.traceId)
        assertEquals(expected.spanId, observed.spanId)
        assertEquals(expected.traceState.asMap(), observed.traceState)
        assertEquals(expected.traceFlags.hex, observed.traceFlags)
    }

    private fun compareStatusData(expected: StatusData, observed: SerializableSpanStatusData) {
        assertEquals(expected.statusCode.name, observed.name)
        assertEquals(expected.description.orEmpty(), observed.description)
    }

    private fun compareAttributes(expected: Map<String, Any>, observed: Map<String, String>) {
        assertEquals(expected.mapValues { it.value.toString() }, observed)
    }

    private fun compareEvents(expected: List<EventData>, observed: List<SerializableEventData>) {
        assertEquals(expected.size, observed.size)

        expected.forEachIndexed { index, data ->
            val observedData = observed[index]
            assertEquals(data.name, observedData.name)
            assertEquals(data.timestamp, observedData.timestamp)
            compareAttributes(data.attributes, observedData.attributes)
        }
    }

    private fun compareLinks(expected: List<LinkData>, observed: List<SerializableLinkData>) {
        assertEquals(expected.size, observed.size)

        expected.forEachIndexed { index, data ->
            val observedData = observed[index]
            compareSpanContexts(data.spanContext, observedData.spanContext)
            compareAttributes(data.attributes, observedData.attributes)
        }
    }

    private fun compareResource(expected: Resource, observed: SerializableResource) {
        assertEquals(expected.schemaUrl, observed.schemaUrl)
        assertEquals(expected.attributes.toMap(), observed.attributes)
    }
}
