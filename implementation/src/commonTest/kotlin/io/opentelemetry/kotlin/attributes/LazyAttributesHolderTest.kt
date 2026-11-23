package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanEventImpl
import io.opentelemetry.kotlin.tracing.SpanLinkImpl
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LazyAttributesHolderTest {

    @Test
    fun `holder without attributes is empty`() {
        val event = SpanEventImpl("event", 1, attributeLimit = 1, attributeValueLengthLimit = 3)
        assertEquals(emptyMap(), event.attributes)
        assertEquals(0, event.droppedAttributesCount)
    }

    @Test
    fun `event applies limits on first write`() {
        val event = SpanEventImpl("event", 1, attributeLimit = 1, attributeValueLengthLimit = 3)
        event.setStringAttribute("foo", "truncated")
        event.setLongAttribute("bar", 2)
        assertEquals(mapOf("foo" to "tru"), event.attributes)
        assertEquals(1, event.droppedAttributesCount)
    }

    @Test
    fun `link applies limits on first write`() {
        val link = SpanLinkImpl(FakeSpanContext.VALID, attributeLimit = 2, attributeValueLengthLimit = 2)
        link.setStringListAttribute("foo", listOf("abc", "d"))
        link.setBooleanAttribute("bar", true)
        link.setDoubleAttribute("baz", 1.0)
        assertEquals(mapOf("foo" to listOf("ab", "d"), "bar" to true), link.attributes)
        assertEquals(1, link.droppedAttributesCount)
    }
}
