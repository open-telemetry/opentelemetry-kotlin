package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class NonRecordingSpanTest {

    @Test
    fun `test non recording span`() {
        val span = NonRecordingSpan(
            FakeSpanContext.INVALID,
            FakeSpanContext.INVALID,
        )
        assertEquals(0, span.startTimestamp)
        assertFalse(span.isRecording())

        span.setStringAttribute("string", "value")
        span.setBooleanAttribute("bool", true)
        span.setLongAttribute("long", 5L)
        span.setDoubleAttribute("double", 3.4)
        span.setStringListAttribute("list", listOf("a", "b"))
        span.setBooleanListAttribute("boolList", listOf(true, false))
        span.setLongListAttribute("longList", listOf(1L, 2L))
        span.setDoubleListAttribute("doubleList", listOf(1.1, 2.2))
        assertEquals(emptyMap(), span.attributes)

        span.addEvent("test")
        assertTrue(span.events.isEmpty())
        span.addLink(FakeSpanContext.INVALID)
        assertTrue(span.links.isEmpty())

        span.end()
        span.end(5)
        assertFalse(span.isRecording())
    }
}
