package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.exceptionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SpanExtTest {

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

    @Test
    fun testSpanWrapOperation() {
        val span = FakeSpan("span")
        val updatedName = "override"

        span.wrapOperation {
            assertTrue(span.isRecording())
            span.setName(updatedName)
            StatusData.Ok
        }
        assertFalse(span.isRecording())
        assertEquals(StatusData.Ok, span.status)
        assertEquals(updatedName, span.name)
    }

    @Test
    fun testSpanWrapOperationFailure() {
        val span = FakeSpan("span")
        val error = StatusData.Error("oops")

        span.wrapOperation {
            assertTrue(span.isRecording())
            error
        }
        assertFalse(span.isRecording())
        assertTrue(span.events.isEmpty())
        assertEquals(error, span.status)
    }

    @Test
    fun testSpanWrapOperationThrows() {
        val span = FakeSpan("span")
        val errMessage = "Whoops"
        val exc = IllegalStateException(errMessage)

        span.wrapOperation {
            throw exc
        }

        assertFalse(span.isRecording())
        assertTrue(span.status is StatusData.Error)
        assertEquals(errMessage, span.status.description)

        val event = span.events.single()
        assertEquals("exception", event.name)

        val attrs = event.attributes
        assertEquals(3, attrs.size)
        assertEquals(exc.exceptionType(), attrs["exception.type"])
        assertEquals("Whoops", attrs["exception.message"])
        assertNotNull(attrs["exception.stacktrace"])
    }
}
