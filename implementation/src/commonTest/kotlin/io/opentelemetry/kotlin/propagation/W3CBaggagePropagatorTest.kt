package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageEntryMetadataImpl
import io.opentelemetry.kotlin.baggage.BaggageImpl
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class W3CBaggagePropagatorTest {

    private val propagator = W3CBaggagePropagator
    private val contextFactory = ContextFactoryImpl()

    @Test
    fun `fields returns only the baggage header`() {
        assertEquals(listOf("baggage"), propagator.fields().toList())
    }

    @Test
    fun `inject does nothing when baggage is empty`() {
        val carrier = mutableMapOf<String, String>()
        propagator.inject(contextFactory.root(), carrier, MapTextMapSetter)
        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `inject writes a single entry`() {
        val carrier = injectInto(BaggageImpl.EMPTY.set("k", "v"))
        assertEquals("k=v", carrier["baggage"])
    }

    @Test
    fun `inject preserves entry insertion order`() {
        val baggage = BaggageImpl.EMPTY.set("a", "1").set("b", "2").set("c", "3")
        val carrier = injectInto(baggage)
        assertEquals("a=1,b=2,c=3", carrier["baggage"])
    }

    @Test
    fun `inject percent-encodes reserved characters in value`() {
        val baggage = BaggageImpl.EMPTY.set("k", "hello world,with;chars")
        val carrier = injectInto(baggage)
        assertEquals("k=hello%20world%2Cwith%3Bchars", carrier["baggage"])
    }

    @Test
    fun `inject percent-encodes the percent character`() {
        val baggage = BaggageImpl.EMPTY.set("k", "100%")
        val carrier = injectInto(baggage)
        assertEquals("k=100%25", carrier["baggage"])
    }

    @Test
    fun `inject percent-encodes utf-8 multibyte characters`() {
        val baggage = BaggageImpl.EMPTY.set("k", "café")
        val carrier = injectInto(baggage)
        assertEquals("k=caf%C3%A9", carrier["baggage"])
    }

    @Test
    fun `inject appends metadata when non-empty`() {
        val baggage = BaggageImpl.EMPTY.set("k", "v", BaggageEntryMetadataImpl("propagation=public"))
        val carrier = injectInto(baggage)
        assertEquals("k=v;propagation=public", carrier["baggage"])
    }

    @Test
    fun `inject omits metadata separator when metadata is empty`() {
        val baggage = BaggageImpl.EMPTY.set("k", "v", BaggageEntryMetadataImpl(""))
        val carrier = injectInto(baggage)
        assertEquals("k=v", carrier["baggage"])
    }

    @Test
    fun `inject skips entry whose key is not a valid token`() {
        val baggage = BaggageImpl.EMPTY.set("good", "g").set("bad key", "b")
        val carrier = injectInto(baggage)
        assertEquals("good=g", carrier["baggage"])
    }

    @Test
    fun `inject skips entry whose serialized form exceeds per-entry limit`() {
        val oversize = "x".repeat(5000)
        val baggage = BaggageImpl.EMPTY.set("ok", "v").set("big", oversize)
        val carrier = injectInto(baggage)
        assertEquals("ok=v", carrier["baggage"])
    }

    @Test
    fun `inject truncates when total header would exceed 8192 bytes`() {
        val builder = (0 until 20).fold(BaggageImpl.EMPTY) { acc, idx ->
            acc.set("k$idx", "v".repeat(500))
        }
        val carrier = injectInto(builder)
        val header = carrier["baggage"]
        assertTrue(header != null)
        assertTrue(header.length <= 8192)
        assertTrue(header.startsWith("k0=vv"))
    }

    @Test
    fun `inject does not write header when nothing fits`() {
        val baggage = BaggageImpl.EMPTY.set("oversized", "x".repeat(5000))
        val carrier = injectInto(baggage)
        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `inject caps at 180 entries even when bytes remain`() {
        val baggage = (0 until 181).fold(BaggageImpl.EMPTY) { acc, idx ->
            acc.set("k$idx", "v")
        }
        val carrier = injectInto(baggage)
        val header = carrier["baggage"]
        assertNotNull(header)
        assertEquals(180, header.split(",").size)
    }

    @Test
    fun `inject skips entry with empty name`() {
        val baggage = BaggageImpl.EMPTY.set("", "v").set("ok", "g")
        val carrier = injectInto(baggage)
        assertEquals("ok=g", carrier["baggage"])
    }

    @Test
    fun `inject preserves baggage-octets in the hash-to-plus range`() {
        val baggage = BaggageImpl.EMPTY.set("k", "#\$&'()*+")
        val carrier = injectInto(baggage)
        assertEquals("k=#\$&'()*+", carrier["baggage"])
    }

    @Test
    fun `inject preserves uppercase token characters in keys`() {
        val baggage = BaggageImpl.EMPTY.set("X-Trace-Id", "v")
        val carrier = injectInto(baggage)
        assertEquals("X-Trace-Id=v", carrier["baggage"])
    }

    @Test
    fun `extract returns original context when header is absent`() {
        val ctx = contextFactory.root()
        val result = propagator.extract(ctx, emptyMap(), MapTextMapGetter)
        assertSame(ctx, result)
    }

    @Test
    fun `extract parses a single entry`() {
        val baggage = extract("k=v")
        assertEquals("v", baggage.getValue("k"))
    }

    @Test
    fun `extract parses multiple comma-separated entries`() {
        val baggage = extract("a=1,b=2,c=3")
        assertEquals("1", baggage.getValue("a"))
        assertEquals("2", baggage.getValue("b"))
        assertEquals("3", baggage.getValue("c"))
    }

    @Test
    fun `extract trims optional whitespace around delimiters`() {
        val baggage = extract(" a = 1 , b = 2 ")
        assertEquals("1", baggage.getValue("a"))
        assertEquals("2", baggage.getValue("b"))
    }

    @Test
    fun `extract decodes percent-encoded utf-8 values`() {
        val baggage = extract("k=caf%C3%A9")
        assertEquals("café", baggage.getValue("k"))
    }

    @Test
    fun `extract preserves metadata opaquely`() {
        val baggage = extract("k=v;propagation=public;ttl=1")
        assertEquals("v", baggage.getValue("k"))
        assertEquals("propagation=public;ttl=1", baggage.asMap()["k"]?.metadata?.value)
    }

    @Test
    fun `extract skips malformed entries but keeps valid ones`() {
        val baggage = extract("ok=v,=missingkey,no-equals,=,k2=v2")
        assertEquals("v", baggage.getValue("ok"))
        assertEquals("v2", baggage.getValue("k2"))
        assertNull(baggage.getValue(""))
        assertEquals(2, baggage.asMap().size)
    }

    @Test
    fun `extract returns original context when all entries are malformed`() {
        val ctx = contextFactory.root()
        val result = propagator.extract(ctx, mapOf("baggage" to "=,;,no-equals"), MapTextMapGetter)
        assertSame(ctx, result)
    }

    @Test
    fun `extract returns original context for invalid percent encoding`() {
        val ctx = contextFactory.root()
        val result = propagator.extract(ctx, mapOf("baggage" to "k=%ZZ"), MapTextMapGetter)
        assertSame(ctx, result)
    }

    @Test
    fun `extract skips empty element between commas`() {
        val baggage = extract("a=1,,b=2")
        assertEquals("1", baggage.getValue("a"))
        assertEquals("2", baggage.getValue("b"))
        assertEquals(2, baggage.asMap().size)
    }

    @Test
    fun `extract skips entry whose key contains an invalid token character`() {
        val baggage = extract("bad key=v,ok=g")
        assertNull(baggage.getValue("bad key"))
        assertEquals("g", baggage.getValue("ok"))
        assertEquals(1, baggage.asMap().size)
    }

    @Test
    fun `extract returns original context for truncated percent encoding`() {
        val ctx = contextFactory.root()
        val result = propagator.extract(ctx, mapOf("baggage" to "k=%A"), MapTextMapGetter)
        assertSame(ctx, result)
    }

    @Test
    fun `extract returns original context when only the low hex digit is invalid`() {
        val ctx = contextFactory.root()
        val result = propagator.extract(ctx, mapOf("baggage" to "k=%AZ"), MapTextMapGetter)
        assertSame(ctx, result)
    }

    @Test
    fun `extract decodes percent-encoded values with lowercase hex`() {
        val baggage = extract("k=caf%c3%a9")
        assertEquals("café", baggage.getValue("k"))
    }

    @Test
    fun `extract combines multiple baggage headers`() {
        val carrier = mapOf("baggage" to listOf("a=1", "b=2,c=3"))
        val ctx = propagator.extract(contextFactory.root(), carrier, MultiValueMapTextMapGetter)
        val baggage = ctx.extractBaggage()
        assertEquals("1", baggage.getValue("a"))
        assertEquals("2", baggage.getValue("b"))
        assertEquals("3", baggage.getValue("c"))
        assertEquals(3, baggage.asMap().size)
    }

    @Test
    fun `extract reads a single baggage header via getAll`() {
        val carrier = mapOf("baggage" to listOf("k=v"))
        val baggage = propagator.extract(contextFactory.root(), carrier, MultiValueMapTextMapGetter)
            .extractBaggage()
        assertEquals("v", baggage.getValue("k"))
        assertEquals(1, baggage.asMap().size)
    }

    @Test
    fun `extract returns original context when getAll returns no headers`() {
        val ctx = contextFactory.root()
        val result = propagator.extract(ctx, emptyMap(), MultiValueMapTextMapGetter)
        assertSame(ctx, result)
    }

    @Test
    fun `inject and extract round-trip preserves entries and metadata`() {
        val original = BaggageImpl.EMPTY
            .set("user.id", "alice", BaggageEntryMetadataImpl("propagation=public"))
            .set("session", "café 1!2@3")
        val carrier = injectInto(original)

        val ctx = contextFactory.root()
        val extracted = propagator.extract(ctx, carrier, MapTextMapGetter).extractBaggage()
        assertEquals("alice", extracted.getValue("user.id"))
        assertEquals("propagation=public", extracted.asMap()["user.id"]?.metadata?.value)
        assertEquals("café 1!2@3", extracted.getValue("session"))
    }

    private fun injectInto(baggage: Baggage): MutableMap<String, String> {
        val carrier = mutableMapOf<String, String>()
        val ctx: Context = contextFactory.root().storeBaggage(baggage)
        propagator.inject(ctx, carrier, MapTextMapSetter)
        return carrier
    }

    private fun extract(header: String): Baggage {
        val ctx = propagator.extract(
            contextFactory.root(),
            mapOf("baggage" to header),
            MapTextMapGetter,
        )
        return ctx.extractBaggage()
    }

    private object MultiValueMapTextMapGetter : TextMapGetter<Map<String, List<String>>> {
        override fun keys(carrier: Map<String, List<String>>): Collection<String> = carrier.keys
        override fun get(carrier: Map<String, List<String>>, key: String): String? =
            carrier[key]?.firstOrNull()
        override fun getAll(carrier: Map<String, List<String>>, key: String): List<String> =
            carrier[key].orEmpty()
    }
}
