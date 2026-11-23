package io.opentelemetry.kotlin.propagation

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class TextMapPropagatorAdapterTest {

    private val impl: OtelJavaTextMapPropagator = W3CBaggagePropagator.getInstance()

    @Test
    fun `fields delegates to wrapped Java propagator`() {
        val adapter = TextMapPropagatorAdapter(impl)
        assertEquals(impl.fields().toList(), adapter.fields().toList())
    }

    @Test
    fun `inject writes baggage header into carrier`() {
        val javaCtx = OtelJavaBaggage.builder()
            .put("k", "v")
            .build()
            .storeInContext(OtelJavaContext.root())
        val ktCtx = javaCtx.toOtelKotlinContext()
        val carrier = mutableMapOf<String, String>()

        TextMapPropagatorAdapter(impl).inject(ktCtx, carrier, MapSetter)

        assertEquals("k=v", carrier["baggage"])
    }

    @Test
    fun `extract reads baggage header from carrier`() {
        val carrier = mapOf("baggage" to "k=v")

        val extracted = TextMapPropagatorAdapter(impl).extract(
            OtelJavaContext.root().toOtelKotlinContext(),
            carrier,
            MapGetter
        )

        val javaCtx = extracted.toOtelJavaContext()
        assertEquals("v", OtelJavaBaggage.fromContext(javaCtx).getEntryValue("k"))
    }

    @Test
    fun `extract exposes extracted baggage to Kotlin`() {
        val extracted = TextMapPropagatorAdapter(impl).extract(
            OtelJavaContext.root().toOtelKotlinContext(),
            mapOf("baggage" to "k=v"),
            MapGetter
        )
        assertEquals("v", extracted.extractBaggage().getValue("k"))
    }

    @Test
    fun `extract on malformed input returns input context unchanged`() {
        val input = OtelJavaContext.root().toOtelKotlinContext()
        val extracted = TextMapPropagatorAdapter(impl).extract(
            input,
            mapOf("baggage" to ""),
            MapGetter
        )
        val javaCtx = extracted.toOtelJavaContext()
        assertTrue(OtelJavaBaggage.fromContext(javaCtx).isEmpty)
    }

    @Test
    fun `inject with empty context does not write carrier`() {
        val carrier = mutableMapOf<String, String>()

        TextMapPropagatorAdapter(impl).inject(FakeContext(), carrier, MapSetter)

        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `extract adapts context that did not come from Java`() {
        var getterCalled = false
        val getter = object : TextMapGetter<Map<String, String>> {
            override fun keys(carrier: Map<String, String>): Collection<String> {
                getterCalled = true
                return carrier.keys
            }

            override fun get(carrier: Map<String, String>, key: String): String? {
                getterCalled = true
                return carrier[key]
            }

            override fun getAll(carrier: Map<String, String>, key: String): List<String> {
                getterCalled = true
                return carrier[key]?.let(::listOf) ?: emptyList()
            }
        }

        val extracted = TextMapPropagatorAdapter(impl).extract(
            FakeContext(),
            mapOf("baggage" to "k=v"),
            getter
        )

        assertTrue(getterCalled)
        assertTrue(extracted is FakeContext)
    }

    @Test
    fun `getter adapter delegates keys to wrapped Kotlin getter`() {
        val carrier = mapOf("a" to "1", "b" to "2")
        val adapter = OtelJavaTextMapGetterAdapter(MapGetter)
        assertEquals(carrier.keys, adapter.keys(carrier).toSet())
    }

    @Test
    fun `setter adapter is a no-op when carrier is null`() {
        val throwingDelegate = TextMapSetter<MutableMap<String, String>> { _, _, _ ->
            error("delegate must not be invoked when carrier is null")
        }
        val adapter = OtelJavaTextMapSetterAdapter(throwingDelegate)
        adapter.set(null, "k", "v")
    }

    @Test
    fun `kotlin getter adapter returns single value list from getAll when key present`() {
        val carrier = mapOf("a" to "1")
        val adapter = TextMapGetterAdapter(JavaMapGetter)
        assertEquals(listOf("1"), adapter.getAll(carrier, "a"))
    }

    @Test
    fun `kotlin getter adapter returns empty list from getAll when key absent`() {
        val carrier = mapOf("a" to "1")
        val adapter = TextMapGetterAdapter(JavaMapGetter)
        assertTrue(adapter.getAll(carrier, "missing").isEmpty())
    }

    @Test
    fun `kotlin getter adapter returns all values from getAll when multiple values present`() {
        val carrier = mapOf("baggage" to listOf("a=1", "b=2"))
        val adapter = TextMapGetterAdapter(JavaMultiValueMapGetter)
        assertEquals(listOf("a=1", "b=2"), adapter.getAll(carrier, "baggage"))
    }

    @Test
    fun `kotlin getter adapter preserves value ordering from getAll`() {
        val carrier = mapOf("baggage" to listOf("c=3", "a=1", "b=2"))
        val adapter = TextMapGetterAdapter(JavaMultiValueMapGetter)
        assertEquals(listOf("c=3", "a=1", "b=2"), adapter.getAll(carrier, "baggage"))
    }

    @Test
    fun `java getter adapter returns all values from getAll when multiple values present`() {
        val carrier = mapOf("baggage" to listOf("a=1", "b=2"))
        val adapter = OtelJavaTextMapGetterAdapter(KotlinMultiValueMapGetter)
        assertEquals(listOf("a=1", "b=2"), adapter.getAll(carrier, "baggage").asSequence().toList())
    }

    @Test
    fun `java getter adapter preserves value ordering from getAll`() {
        val carrier = mapOf("baggage" to listOf("c=3", "a=1", "b=2"))
        val adapter = OtelJavaTextMapGetterAdapter(KotlinMultiValueMapGetter)
        assertEquals(listOf("c=3", "a=1", "b=2"), adapter.getAll(carrier, "baggage").asSequence().toList())
    }

    @Test
    fun `java getter adapter returns empty list from getAll when key absent`() {
        val carrier = mapOf("baggage" to listOf("a=1"))
        val adapter = OtelJavaTextMapGetterAdapter(KotlinMultiValueMapGetter)
        assertTrue(adapter.getAll(carrier, "missing").asSequence().toList().isEmpty())
    }

    @Test
    fun `java getter adapter returns empty list from getAll when carrier is null`() {
        val adapter = OtelJavaTextMapGetterAdapter(KotlinMultiValueMapGetter)
        assertTrue(adapter.getAll(null, "baggage").asSequence().toList().isEmpty())
    }

    @Test
    fun `getter adapter returns null when carrier is null`() {
        val throwingDelegate = object : TextMapGetter<Map<String, String>> {
            val errMsg = "delegate must not be invoked when carrier is null"

            override fun keys(carrier: Map<String, String>): Collection<String> {
                error(errMsg)
            }

            override fun get(carrier: Map<String, String>, key: String): String {
                error(errMsg)
            }

            override fun getAll(carrier: Map<String, String>, key: String): List<String> {
                error(errMsg)
            }
        }
        val adapter = OtelJavaTextMapGetterAdapter(throwingDelegate)

        assertNull(adapter.get(null, "k"))
    }

    private object MapSetter : TextMapSetter<MutableMap<String, String>> {
        override fun set(carrier: MutableMap<String, String>, key: String, value: String) {
            carrier[key] = value
        }
    }

    private object MapGetter : TextMapGetter<Map<String, String>> {
        override fun keys(carrier: Map<String, String>): Collection<String> = carrier.keys
        override fun get(carrier: Map<String, String>, key: String): String? = carrier[key]
        override fun getAll(carrier: Map<String, String>, key: String): List<String> =
            carrier[key]?.let { listOf(it) } ?: emptyList()
    }

    private object JavaMapGetter : OtelJavaTextMapGetter<Map<String, String>> {
        @Deprecated("Overrideing deprecated interface member")
        override fun keys(carrier: Map<String, String>): Iterable<String> = carrier.keys
        override fun get(carrier: Map<String, String>?, key: String): String? = carrier?.get(key)
    }

    private object JavaMultiValueMapGetter : OtelJavaTextMapGetter<Map<String, List<String>>> {
        @Deprecated("Overriding deprecated interface member")
        override fun keys(carrier: Map<String, List<String>>): Iterable<String> = carrier.keys
        override fun get(carrier: Map<String, List<String>>?, key: String): String? =
            carrier?.get(key)?.firstOrNull()
        override fun getAll(carrier: Map<String, List<String>>?, key: String): MutableIterator<String> =
            carrier?.get(key).orEmpty().toMutableList().iterator()
    }

    private object KotlinMultiValueMapGetter : TextMapGetter<Map<String, List<String>>> {
        override fun keys(carrier: Map<String, List<String>>): Collection<String> = carrier.keys
        override fun get(carrier: Map<String, List<String>>, key: String): String? =
            carrier[key]?.firstOrNull()
        override fun getAll(carrier: Map<String, List<String>>, key: String): List<String> =
            carrier[key].orEmpty()
    }
}
