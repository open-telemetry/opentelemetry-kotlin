@file:Suppress("DEPRECATION")

package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.FakeSpanEventData
import io.opentelemetry.kotlin.tracing.data.FakeSpanLinkData
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SpanDataExtTest {

    @Test
    fun testConversion() {
        val input = FakeSpanData()
        val observed = input.toOtelJavaSpanData()
        assertEquals(input.hasEnded, observed.hasEnded())
        assertEquals(input.endTimestamp, observed.endEpochNanos)
        assertEquals(input.events.size, observed.totalRecordedEvents)
        assertEquals(input.links.size, observed.totalRecordedLinks)
        assertEquals(input.attributes.size, observed.totalAttributeCount)
    }

    @Test
    fun testDroppedCountsConversion() {
        val input = FakeSpanData(
            droppedAttributesCount = 3,
            droppedEventsCount = 4,
            droppedLinksCount = 5,
        )
        val observed = input.toOtelJavaSpanData()
        assertEquals(input.attributes.size + 3, observed.totalAttributeCount)
        assertEquals(input.events.size + 4, observed.totalRecordedEvents)
        assertEquals(input.links.size + 5, observed.totalRecordedLinks)
    }

    @Test
    fun testEventAndLinkDroppedAttributesConversion() {
        val input = FakeSpanData(
            events = listOf(FakeSpanEventData(droppedAttributesCount = 2)),
            links = listOf(FakeSpanLinkData(droppedAttributesCount = 6)),
        )
        val observed = input.toOtelJavaSpanData()
        assertEquals(3, observed.events.single().totalAttributeCount)
        assertEquals(7, observed.links.single().totalAttributeCount)
    }

    @Test
    fun testScopeConversion() {
        val scope = FakeInstrumentationScopeInfo(attributes = mapOf("scope.key" to "scope.value"))
        val observed = FakeSpanData(instrumentationScopeInfo = scope).toOtelJavaSpanData()
        assertEquals(scope.toOtelJavaInstrumentationScopeInfo(), observed.instrumentationScopeInfo)

        val libraryInfo = observed.instrumentationLibraryInfo
        assertEquals(scope.name, libraryInfo.name)
        assertEquals(scope.version, libraryInfo.version)
        assertEquals(scope.schemaUrl, libraryInfo.schemaUrl)
    }
}
