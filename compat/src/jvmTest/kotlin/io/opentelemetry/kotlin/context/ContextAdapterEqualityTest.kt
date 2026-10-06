package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.factory.CompatContextFactory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class ContextAdapterEqualityTest {

    private val factory = CompatContextFactory()
    private val key = factory.createKey<String>("key")

    @Test
    fun `root and implicit are equal across calls`() {
        assertEquals(factory.root(), factory.root())
        assertEquals(factory.root().hashCode(), factory.root().hashCode())
        assertEquals(factory.root(), factory.implicit())

        val ctx = factory.root().set(key, "value")
        val scope = ctx.attach()
        assertEquals(ctx, factory.implicit())
        assertEquals(factory.implicit(), factory.implicit())
        scope.detach()
    }

    @Test
    fun `contexts derived separately are not equal`() {
        assertNotEquals(factory.root().set(key, "value"), factory.root().set(key, "value"))
    }

    @Test
    fun `java adapter equality follows wrapped kotlin context`() {
        val ctx = factory.root().set(key, "value")
        assertEquals(OtelJavaContextAdapter(ctx), OtelJavaContextAdapter(ctx))
        assertEquals(OtelJavaContextAdapter(ctx).hashCode(), OtelJavaContextAdapter(ctx).hashCode())

        val javaKey = OtelJavaContextKey.named<String>("other")
        assertNotEquals(OtelJavaContextAdapter(ctx), OtelJavaContextAdapter(ctx).with(javaKey, "v"))
    }
}
