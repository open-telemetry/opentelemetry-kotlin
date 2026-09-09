package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompositeTextMapPropagatorTest {

    private val contextFactory = ContextFactoryImpl(FakeSpanFactory())

    @Test
    fun `fields returns deduped union preserving order`() {
        val a = FakeTextMapPropagator(listOf("a", "shared"))
        val b = FakeTextMapPropagator(listOf("b", "shared", "c"))
        val composite = CompositeTextMapPropagator(listOf(a, b))
        assertEquals(listOf("a", "shared", "b", "c"), composite.fields().toList())
    }

    @Test
    fun `fields is empty when there are no delegates`() {
        assertEquals(emptyList(), CompositeTextMapPropagator(emptyList()).fields().toList())
    }

    @Test
    fun `inject invokes all delegates in order`() {
        val a = FakeTextMapPropagator(listOf("a"))
        val b = FakeTextMapPropagator(listOf("b"))
        val composite = CompositeTextMapPropagator(listOf(a, b))
        val carrier = mutableMapOf<String, String>()

        composite.inject(contextFactory.root(), carrier, FakeTextMapSetter)

        assertEquals(listOf("a", "b"), carrier.keys.toList())
        assertTrue(a.injectCalled)
        assertTrue(b.injectCalled)
    }

    @Test
    fun `extract threads context through delegates left-to-right`() {
        val keyA: ContextKey<String> = contextFactory.createKey("a")
        val keyB: ContextKey<String> = contextFactory.createKey("b")
        val composite = CompositeTextMapPropagator(
            listOf(
                FakeTextMapPropagator(onExtract = { it.set(keyA, "alpha") }),
                FakeTextMapPropagator(onExtract = { it.set(keyB, "beta") }),
            ),
        )

        val result = composite.extract(contextFactory.root(), emptyMap(), FakeTextMapGetter)
        assertEquals("alpha", result.get(keyA))
        assertEquals("beta", result.get(keyB))
    }

    @Test
    fun `extract returns the original context when there are no delegates`() {
        val ctx = contextFactory.root()
        assertSame(ctx, CompositeTextMapPropagator(emptyList()).extract(ctx, emptyMap(), FakeTextMapGetter))
    }

    @Test
    fun `later delegate sees the context written by an earlier one`() {
        val key: ContextKey<String> = contextFactory.createKey("k")
        var observed: String? = null
        val composite = CompositeTextMapPropagator(
            listOf(
                FakeTextMapPropagator(onExtract = { it.set(key, "first") }),
                FakeTextMapPropagator(onExtract = { ctx -> ctx.also { observed = ctx.get(key) } }),
            ),
        )
        composite.extract(contextFactory.root(), emptyMap(), FakeTextMapGetter)
        assertEquals("first", observed)
    }
}
