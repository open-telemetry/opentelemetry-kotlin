package io.opentelemetry.kotlin.context

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
        val repository = OtelJavaContextKeyRepository()
        val ctx = OtelJavaContextAdapter(contextFactory.root(), repository)
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
}
