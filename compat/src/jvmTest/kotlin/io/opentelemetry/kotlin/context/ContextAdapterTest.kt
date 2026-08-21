package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class ContextAdapterTest {

    @Test
    fun testScope() {
        val ctx = ContextAdapter(OtelJavaContext.root())
        val scope = ctx.attach()
        assertNotNull(scope)
        scope.detach()
    }

    @Test
    fun `set with null value is a no-op`() {
        val key = ContextKeyAdapter<String>(OtelJavaContextKey.named("key"))
        val ctx = ContextAdapter(OtelJavaContext.root())

        val result = ctx.set(key, null)

        assertSame(ctx, result)
        assertNull(result.get(key))
    }

    @Test
    fun `set with null value retains the existing value`() {
        val key = ContextKeyAdapter<String>(OtelJavaContextKey.named("key"))
        val ctx = ContextAdapter(OtelJavaContext.root()).set(key, "value")

        val result = ctx.set(key, null)

        assertSame(ctx, result)
        assertEquals("value", result.get(key))
    }
}
