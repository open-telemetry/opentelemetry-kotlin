package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.RecordingSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BaggageImplTest {

    private val errorHandler = RecordingSdkErrorHandler()

    @BeforeTest
    fun setUp() {
        BaggageImpl.entryLimitReported.set(false)
        errorHandler.install()
    }

    @AfterTest
    fun tearDown() {
        errorHandler.uninstall()
    }

    @Test
    fun `getValue returns value for present key`() {
        val baggage = BaggageImpl.EMPTY.put("key", "value")
        assertEquals("value", baggage.getValue("key"))
    }

    @Test
    fun `getValue returns null for absent key`() {
        assertNull(BaggageImpl.EMPTY.getValue("missing"))
    }

    @Test
    fun `asMap returns all entries`() {
        val baggage = BaggageImpl.EMPTY.put("k", "v")
        val map = baggage.asMap()
        assertEquals(1, map.size)
        assertEquals("v", map["k"]?.value)
    }

    @Test
    fun `put without metadata adds entry and leaves original unchanged`() {
        val original = BaggageImpl.EMPTY
        val updated = original.put("foo", "bar")
        assertEquals("bar", updated.getValue("foo"))
        assertNull(original.getValue("foo"))
    }

    @Test
    fun `put with metadata preserves metadata`() {
        val updated = BaggageImpl.EMPTY.put("foo", "bar", "myMeta")
        assertEquals("bar", updated.getValue("foo"))
        assertEquals("myMeta", updated.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `put without metadata uses empty metadata`() {
        val updated = BaggageImpl.EMPTY.put("foo", "bar")
        assertEquals("", updated.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `put without metadata clears existing metadata`() {
        val original = createBaggage().put("foo", "bar", "myMeta")
        val updated = original.put("foo", "baz")
        assertEquals("baz", updated.getValue("foo"))
        assertEquals("", updated.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `put replaces existing value`() {
        val original = BaggageImpl.EMPTY.put("key", "old")
        val updated = original.put("key", "new")
        assertEquals("new", updated.getValue("key"))
    }

    @Test
    fun `remove deletes existing entry`() {
        val original = BaggageImpl.EMPTY.put("key", "val")
        val updated = original.remove("key")
        assertNull(updated.getValue("key"))
    }

    @Test
    fun `remove absent key returns same instance`() {
        val original = BaggageImpl.EMPTY.put("key", "val")
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
        val baggage = BaggageImpl.EMPTY.put("k", "v")
        assertTrue(baggage.asMap()["k"] is BaggageEntryImpl)
    }

    @Test
    fun `put accepts non-token keys and values with special characters`() {
        val baggage = BaggageImpl.EMPTY
            .put("bad key", "line1\nline2")
            .put("another,key;", "val\r\u0000")
            .put("", "empty key")
        assertEquals("line1\nline2", baggage.getValue("bad key"))
        assertEquals("val\r\u0000", baggage.getValue("another,key;"))
        assertEquals("empty key", baggage.getValue(""))
        assertEquals(3, baggage.asMap().size)
    }

    @Test
    fun `put with metadata accepts non-token keys`() {
        val baggage = BaggageImpl.EMPTY.put("bad key", "v", "m")
        assertEquals("v", baggage.getValue("bad key"))
        assertEquals("m", baggage.asMap()["bad key"]?.metadata?.value)
    }

    @Test
    fun `put drops new entry beyond MAX_ENTRIES`() {
        val full = fullBaggage()
        assertEquals(BaggageImpl.MAX_ENTRIES, full.asMap().size)
        val attempted = full.put("kExtra", "v")
        assertSame(full, attempted)
        assertNull(attempted.getValue("kExtra"))
    }

    @Test
    fun `put reports the first entry dropped beyond MAX_ENTRIES`() {
        val full = fullBaggage()
        assertEquals(emptyList(), errorHandler.errors)

        full.put("kExtra", "v")

        val error = assertIs<SdkError.ApiMisuse>(errorHandler.errors.single())
        assertEquals("Baggage.put", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun `put does not report later entries dropped beyond MAX_ENTRIES`() {
        val full = fullBaggage()
        full.put("kExtra1", "v")
        full.put("kExtra2", "v")
        fullBaggage().put("kExtra3", "v", "m")
        assertEquals(1, errorHandler.errors.size, "errors: ${errorHandler.errors}")
    }

    @Test
    fun `put replaces existing key when at MAX_ENTRIES cap`() {
        val full = fullBaggage()
        val updated = full.put("k0", "new")
        assertEquals(BaggageImpl.MAX_ENTRIES, updated.asMap().size)
        assertEquals("new", updated.getValue("k0"))
        assertEquals(emptyList(), errorHandler.errors)
    }

    @Test
    fun `equal when entries match regardless of insertion order`() {
        val a = BaggageImpl.EMPTY.put("k1", "v1").put("k2", "v2")
        val b = BaggageImpl.EMPTY.put("k2", "v2").put("k1", "v1")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `not equal when value differs`() {
        assertNotEquals(BaggageImpl.EMPTY.put("k", "v1"), BaggageImpl.EMPTY.put("k", "v2"))
    }

    @Test
    fun `not equal when metadata differs`() {
        val a = BaggageImpl.EMPTY.put("k", "v", "m1")
        val b = BaggageImpl.EMPTY.put("k", "v", "m2")
        assertNotEquals(a, b)
    }

    @Test
    fun `put then remove equals original`() {
        val original = BaggageImpl.EMPTY.put("k1", "v1")
        assertEquals(original, original.put("k2", "v2").remove("k2"))
    }

    private fun fullBaggage(): Baggage =
        (0 until BaggageImpl.MAX_ENTRIES).fold(BaggageImpl.EMPTY) { acc, idx ->
            acc.put("k$idx", "v")
        }
}
