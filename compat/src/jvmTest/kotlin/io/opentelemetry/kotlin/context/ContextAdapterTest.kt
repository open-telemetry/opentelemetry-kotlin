package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ContextAdapterTest {

    @Test
    fun testScope() {
        val ctx = ContextAdapter(OtelJavaContext.root())
        val scope = ctx.attach()
        assertNotNull(scope)
        scope.detach()
    }

    @Test
    fun `set with null value on an absent key`() {
        val key = ContextKeyAdapter<String>(OtelJavaContextKey.named("key"))
        val ctx = ContextAdapter(OtelJavaContext.root())

        val result = ctx.set(key, null)

        assertNull(result.get(key))
    }

    @Test
    fun `set with null value clears the existing value`() {
        val javaKey = OtelJavaContextKey.named<String>("key")
        val key = ContextKeyAdapter(javaKey)
        val ctx = ContextAdapter(OtelJavaContext.root()).set(key, "value")
        assertEquals("value", ctx.get(key))

        val result = ctx.set(key, null)

        assertNull(result.get(key))
        assertNull(result.toOtelJavaContext().get(javaKey))
        assertEquals("value", ctx.get(key))
    }
}
