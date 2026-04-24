package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BaggageImplTest {

    @Test
    fun `getValue returns value for present key`() {
        val baggage = BaggageImpl.EMPTY.set("key", "value")
        assertEquals("value", baggage.getValue("key"))
    }

    @Test
    fun `getValue returns null for absent key`() {
        assertNull(BaggageImpl.EMPTY.getValue("missing"))
    }

    @Test
    fun `asMap returns all entries`() {
        val baggage = BaggageImpl.EMPTY.set("k", "v")
        val map = baggage.asMap()
        assertEquals(1, map.size)
        assertEquals("v", map["k"]?.value)
    }

    @Test
    fun `set without metadata adds entry and leaves original unchanged`() {
        val original = BaggageImpl.EMPTY
        val updated = original.set("foo", "bar")
        assertEquals("bar", updated.getValue("foo"))
        assertNull(original.getValue("foo"))
    }

    @Test
    fun `set with metadata preserves metadata`() {
        val meta = BaggageEntryMetadataImpl("myMeta")
        val updated = BaggageImpl.EMPTY.set("foo", "bar", meta)
        assertEquals("bar", updated.getValue("foo"))
        assertEquals("myMeta", updated.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `set without metadata uses empty metadata`() {
        val updated = BaggageImpl.EMPTY.set("foo", "bar")
        assertEquals("", updated.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `set replaces existing value`() {
        val original = BaggageImpl.EMPTY.set("key", "old")
        val updated = original.set("key", "new")
        assertEquals("new", updated.getValue("key"))
    }

    @Test
    fun `remove deletes existing entry`() {
        val original = BaggageImpl.EMPTY.set("key", "val")
        val updated = original.remove("key")
        assertNull(updated.getValue("key"))
    }

    @Test
    fun `remove absent key returns same instance`() {
        val original = BaggageImpl.EMPTY.set("key", "val")
        val result = original.remove("missing")
        assertSame(original, result)
    }

    @Test
    fun `BaggageEntryImpl delegates value and metadata`() {
        val meta = BaggageEntryMetadataImpl("m")
        val entry = BaggageEntryImpl("v", meta)
        assertEquals("v", entry.value)
        assertSame(meta, entry.metadata)
    }

    @Test
    fun `BaggageEntryMetadataImpl delegates value`() {
        val meta = BaggageEntryMetadataImpl("someValue")
        assertEquals("someValue", meta.value)
    }

    @Test
    fun `asMap entries are BaggageEntryImpl instances`() {
        val baggage = BaggageImpl.EMPTY.set("k", "v")
        assertTrue(baggage.asMap()["k"] is BaggageEntryImpl)
    }
}
