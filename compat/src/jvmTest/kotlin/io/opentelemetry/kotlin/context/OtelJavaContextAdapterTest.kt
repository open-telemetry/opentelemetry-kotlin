package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.factory.CompatContextFactory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

internal class OtelJavaContextAdapterTest {

    private val contextFactory = CompatContextFactory()

    @Test
    fun `test context`() {
        val ctx = OtelJavaContextAdapter(contextFactory.root())
        val key1 = OtelJavaContextKey.named<String>("foo")
        val key2 = OtelJavaContextKey.named<String>("foo")
        val key3 = OtelJavaContextKey.named<String>("bar")

        assertNull(ctx.get(key1))
        assertNull(ctx.get(key2))
        assertNull(ctx.get(key3))

        val newCtx = ctx.with(key1, "value1")
        assertNotSame(ctx, newCtx)
        assertEquals("value1", newCtx.get(key1))
        assertNull(newCtx.get(key2))
        assertNull(newCtx.get(key3))
    }

    @Test
    fun `with null value clears the existing value`() {
        val ctx = OtelJavaContextAdapter(contextFactory.root())
        val key = OtelJavaContextKey.named<String>("foo")
        val withValue = ctx.with(key, "value1")
        val result = withValue.with(key, javaNull())

        assertNull(result.get(key))
        assertEquals("value1", withValue.get(key))
    }

    /**
     * Simulates a Java caller passing null, bypassing Kotlin's non-null view of the parameter.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <R> javaNull(): R = null as R

    @Test
    fun `values round trip through a non-compat context`() {
        val ctx = OtelJavaContextAdapter(MapContext(emptyMap()))
        val key1 = OtelJavaContextKey.named<String>("foo")
        val key2 = OtelJavaContextKey.named<String>("foo")

        val newCtx = ctx.with(key1, "value1")
        assertEquals("value1", newCtx.get(key1))
        assertNull(newCtx.get(key2))
    }

    private class MapContext(
        private val values: Map<ContextKey<*>, Any?>
    ) : Context by ContextAdapter(OtelJavaContext.root()) {
        override fun <T> set(key: ContextKey<T>, value: T?): Context = MapContext(values + (key to value))

        @Suppress("UNCHECKED_CAST")
        override fun <T> get(key: ContextKey<T>): T? = values[key] as T?
    }
}
