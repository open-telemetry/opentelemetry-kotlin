package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

internal class OtelJavaSpanAdapterTest {

    private val typedAttrs: OtelJavaAttributes = OtelJavaAttributes.builder()
        .put("str", "hello")
        .put("long", 42L)
        .put("double", 42.0)
        .put("bool", true)
        .put(AttributeKey.stringArrayKey("strList"), listOf("a", "b"))
        .put(AttributeKey.longArrayKey("longList"), listOf(1L, 2L))
        .put(AttributeKey.doubleArrayKey("doubleList"), listOf(1.0, 2.0))
        .put(AttributeKey.booleanArrayKey("boolList"), listOf(true, false))
        .build()

    private fun assertTypesPreserved(attrs: Map<String, Any>) {
        assertEquals("hello", attrs["str"])
        assertEquals(42L, attrs["long"])
        // A whole-valued double must not collapse to a long, nor to the string "42.0".
        assertEquals(42.0, attrs["double"])
        assertEquals(true, attrs["bool"])
        assertEquals(listOf("a", "b"), attrs["strList"])
        assertEquals(listOf(1L, 2L), attrs["longList"])
        assertEquals(listOf(1.0, 2.0), attrs["doubleList"])
        assertEquals(listOf(true, false), attrs["boolList"])
    }

    @Test
    fun testSetAttributePreservesTypes() {
        val span = FakeSpan()
        val adapter = OtelJavaSpanAdapter(span)

        adapter.setAllAttributes(typedAttrs)

        assertTypesPreserved(span.attributes)
    }

    @Test
    fun testSetAttributeDropsNull() {
        val span = FakeSpan()
        val adapter = OtelJavaSpanAdapter(span)

        adapter.setAttribute(AttributeKey.stringKey("nullable"), null)

        // A null value is dropped rather than recorded as the string "null".
        assertFalse(span.attributes.containsKey("nullable"))
        assertNull(span.attributes["nullable"])
    }

    @Test
    fun testAddEventPreservesTypes() {
        val span = FakeSpan()
        val adapter = OtelJavaSpanAdapter(span)

        adapter.addEvent("event", typedAttrs)

        assertEquals(1, span.events.size)
        assertEquals("event", span.events.single().name)
        assertTypesPreserved(span.events.single().attributes)
    }

    @Test
    fun testAddLinkPreservesTypes() {
        val span = FakeSpan()
        val adapter = OtelJavaSpanAdapter(span)

        adapter.addLink(FakeSpanContext.INVALID.toOtelJavaSpanContext(), typedAttrs)

        assertEquals(1, span.links.size)
        assertTypesPreserved(span.links.single().attributes)
    }

    @Test
    fun testRecordExceptionPreservesTypes() {
        val span = FakeSpan()
        val adapter = OtelJavaSpanAdapter(span)

        adapter.recordException(IllegalStateException("boom"), typedAttrs)

        val event = span.events.single()
        assertEquals("exception", event.name)
        assertTypesPreserved(event.attributes)
        // The exception attributes are still applied alongside the additional ones.
        assertEquals("boom", event.attributes["exception.message"])
    }
}
