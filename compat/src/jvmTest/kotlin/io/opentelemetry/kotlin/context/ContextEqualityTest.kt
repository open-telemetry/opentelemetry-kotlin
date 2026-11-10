package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.framework.SdkTracker
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class ContextEqualityTest {

    private val factory = CompatContextFactory()

    private val sdks = SdkTracker().also { createCompatOpenTelemetry().also(it::track) }

    @AfterTest
    fun tearDown() = sdks.shutdownAll()
    private val key = createContextKey<String>("key")

    @Test
    fun `root and current are equal across calls`() {
        assertEquals(factory.rootContext(), factory.rootContext())
        assertEquals(factory.rootContext().hashCode(), factory.rootContext().hashCode())
        assertEquals(factory.rootContext(), factory.currentContext())

        val ctx = factory.rootContext().set(key, "value")
        val scope = ctx.attach()
        assertEquals(ctx, factory.currentContext())
        assertEquals(factory.currentContext(), factory.currentContext())
        scope.detach()
    }

    @Test
    fun `contexts derived separately are not equal`() {
        assertNotEquals(factory.rootContext().set(key, "value"), factory.rootContext().set(key, "value"))
    }

    @Test
    fun `java adapter equality follows wrapped kotlin context`() {
        val ctx = factory.rootContext().set(key, "value")
        assertEquals(OtelJavaContextAdapter(ctx), OtelJavaContextAdapter(ctx))
        assertEquals(OtelJavaContextAdapter(ctx).hashCode(), OtelJavaContextAdapter(ctx).hashCode())

        val javaKey = OtelJavaContextKey.named<String>("other")
        assertNotEquals(OtelJavaContextAdapter(ctx), OtelJavaContextAdapter(ctx).with(javaKey, "v"))
    }
}
