package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.exceptionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanExtTest {

    @Test
    fun testRecordException() {
        val span = FakeSpan()
        assertEquals(0, span.events.size)

        val exc = IllegalArgumentException()
        span.recordException(exc)
        val exc1 = IllegalStateException("Whoops!")
        span.recordException(exc1) {
            setStringAttribute("extra", "value")
        }
        val events = span.events
        assertEquals(2, events.size)

        val simple = events.first()
        assertEquals("exception", simple.name)
        val simpleAttrs = simple.attributes
        assertEquals(2, simpleAttrs.size)
        assertEquals(exc.exceptionType(), simpleAttrs["exception.type"])
        assertNotNull(simpleAttrs["exception.stacktrace"])

        val complex = events.last()
        assertEquals("exception", complex.name)
        val complexAttrs = complex.attributes
        assertEquals(4, complexAttrs.size)
        assertEquals(exc1.exceptionType(), complexAttrs["exception.type"])
        assertEquals("Whoops!", complexAttrs["exception.message"])
        assertEquals("value", complexAttrs["extra"])
        assertNotNull(complexAttrs["exception.stacktrace"])
    }

    @Test
    fun testRecordExceptionNoClassName() {
        val span = FakeSpan()
        val exc = object : IllegalArgumentException() {}
        span.recordException(exc)

        val event = span.events.single()
        assertEquals("exception", event.name)

        val simpleAttrs = event.attributes
        assertEquals(1, simpleAttrs.size)
        assertNull(simpleAttrs["exception.type"])
        assertNotNull(simpleAttrs["exception.stacktrace"])
    }

    @Test
    fun testAddLinkSimple() {
        val a = FakeSpan()
        val b = FakeSpan()

        a.addLink(b)
        val link = a.links.single()
        assertEquals(b.spanContext, link.spanContext)
        assertTrue(link.attributes.isEmpty())
    }

    @Test
    fun testAddLinkWithAttrs() {
        val a = FakeSpan()
        val b = FakeSpan()

        a.addLink(b) {
            setStringAttribute("extra", "value")
        }
        val link = a.links.single()
        assertEquals(b.spanContext, link.spanContext)
        assertEquals(mapOf("extra" to "value"), link.attributes)
    }
}
