package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BaggageAdapterTest {

    @Test
    fun `getValue returns value for present key`() {
        val baggage = BaggageAdapter(otelJavaBaggage("key" to "value"))
        assertEquals("value", baggage.getValue("key"))
    }

    @Test
    fun `getValue returns null for absent key`() {
        val baggage = BaggageAdapter(otelJavaBaggage())
        assertNull(baggage.getValue("missing"))
    }

    @Test
    fun `asMap wraps entries as BaggageEntryAdapter`() {
        val baggage = BaggageAdapter(otelJavaBaggage("k" to "v"))
        val map = baggage.asMap()
        assertEquals(1, map.size)
        val entry = map["k"]
        assertTrue(entry is BaggageEntryAdapter)
        assertEquals("v", entry.value)
    }

    @Test
    fun `set without metadata adds entry`() {
        val original = BaggageAdapter(otelJavaBaggage())
        val updated = original.set("foo", "bar")
        assertEquals("bar", updated.getValue("foo"))
        assertNull(original.getValue("foo"))
    }

    @Test
    fun `set with metadata preserves metadata`() {
        val meta = object : BaggageEntryMetadata {
            override val value = "myMeta"
        }
        val updated = BaggageAdapter(otelJavaBaggage()).set("foo", "bar", meta)
        assertEquals("bar", updated.getValue("foo"))
        val entry = checkNotNull(updated.asMap()["foo"])
        assertEquals("myMeta", entry.metadata.value)
    }

    @Test
    fun `set replaces existing value`() {
        val original = BaggageAdapter(otelJavaBaggage("key" to "old"))
        val updated = original.set("key", "new")
        assertEquals("new", updated.getValue("key"))
    }

    @Test
    fun `remove deletes existing entry`() {
        val original = BaggageAdapter(otelJavaBaggage("key" to "val"))
        val updated = original.remove("key")
        assertNull(updated.getValue("key"))
    }

    @Test
    fun `remove absent key returns same instance`() {
        val original = BaggageAdapter(otelJavaBaggage("key" to "val"))
        val result = original.remove("missing")
        assertSame(original, result)
    }

    @Test
    fun `BaggageEntryAdapter delegates value`() {
        val map = otelJavaBaggage("k" to "v").asMap()
        val javaEntry = checkNotNull(map["k"])
        val entry = BaggageEntryAdapter(javaEntry)
        assertEquals("v", entry.value)
    }

    @Test
    fun `BaggageEntryAdapter wraps metadata as BaggageEntryMetadataAdapter`() {
        val javaEntry = OtelJavaBaggage.builder()
            .put("k", "v", OtelJavaBaggageEntryMetadata.create("meta"))
            .build()
            .asMap()["k"]
        val entry = BaggageEntryAdapter(checkNotNull(javaEntry))
        assertTrue(entry.metadata is BaggageEntryMetadataAdapter)
        assertEquals("meta", entry.metadata.value)
    }

    @Test
    fun `BaggageEntryMetadataAdapter delegates value`() {
        val javaMeta = OtelJavaBaggageEntryMetadata.create("someValue")
        val meta = BaggageEntryMetadataAdapter(javaMeta)
        assertEquals("someValue", meta.value)
    }

    private fun otelJavaBaggage(vararg entries: Pair<String, String>): OtelJavaBaggage {
        val builder = OtelJavaBaggage.builder()
        entries.forEach { (k, v) -> builder.put(k, v) }
        return builder.build()
    }
}
