package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.context.FakeContextKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompositePropagatorEntrypointTest {

    @Test
    fun `fields returns deduped union preserving order`() {
        val a = FakeTextMapPropagator(listOf("a", "shared"))
        val b = FakeTextMapPropagator(listOf("b", "shared", "c"))
        val composite = createCompositePropagator(a, b)
        assertEquals(listOf("a", "shared", "b", "c"), composite.fields().toList())
    }

    @Test
    fun `fields is empty when there are no delegates`() {
        assertEquals(emptyList(), createCompositePropagator().fields().toList())
    }

    @Test
    fun `inject invokes all delegates in order`() {
        val a = FakeTextMapPropagator(listOf("a"))
        val b = FakeTextMapPropagator(listOf("b"))
        val composite = createCompositePropagator(a, b)
        val carrier = mutableMapOf<String, String>()

        composite.inject(FakeContext(), carrier, FakeTextMapSetter)

        assertEquals(listOf("a", "b"), carrier.keys.toList())
        assertTrue(a.injectCalled)
        assertTrue(b.injectCalled)
    }

    @Test
    fun `inject with no delegates leaves the carrier untouched`() {
        val carrier = mutableMapOf("key" to "value")
        createCompositePropagator().inject(FakeContext(), carrier, FakeTextMapSetter)
        assertEquals(mapOf("key" to "value"), carrier)
    }

    @Test
    fun `extract threads context through delegates left-to-right`() {
        val keyA = FakeContextKey<String>("a")
        val keyB = FakeContextKey<String>("b")
        val composite = createCompositePropagator(
            FakeTextMapPropagator(onExtract = { it.set(keyA, "alpha") }),
            FakeTextMapPropagator(onExtract = { it.set(keyB, "beta") }),
        )

        val result = composite.extract(FakeContext(), emptyMap(), FakeTextMapGetter)
        assertIs<FakeContext>(result)
        assertEquals<Map<ContextKey<*>, Any?>>(mapOf(keyA to "alpha", keyB to "beta"), result.attrs)
    }

    @Test
    fun `extract returns the original context when there are no delegates`() {
        val ctx = FakeContext()
        assertSame(ctx, createCompositePropagator().extract(ctx, emptyMap(), FakeTextMapGetter))
    }

    @Test
    fun `later delegate sees the context written by an earlier one`() {
        val key = FakeContextKey<String>("k")
        var observed: Any? = null
        val composite = createCompositePropagator(
            FakeTextMapPropagator(onExtract = { it.set(key, "first") }),
            FakeTextMapPropagator(onExtract = { ctx -> ctx.also { observed = (ctx as FakeContext).attrs[key] } }),
        )
        composite.extract(FakeContext(), emptyMap(), FakeTextMapGetter)
        assertEquals("first", observed)
    }
}
