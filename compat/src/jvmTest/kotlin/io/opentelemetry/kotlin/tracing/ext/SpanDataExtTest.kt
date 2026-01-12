package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalApi::class)
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
}
