package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.framework.SdkTracker
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class ContextEqualityTest {

    private val sdks = SdkTracker().also { createCompatOpenTelemetry().also(it::track) }

    @AfterTest
    fun tearDown() = sdks.shutdownAll()
    private val key = createContextKey<String>("key")

    @Test
    fun `root and current are equal across calls`() {
        assertEquals(rootContext(), rootContext())
        assertEquals(rootContext().hashCode(), rootContext().hashCode())
        assertEquals(rootContext(), currentContext())

        val ctx = rootContext().set(key, "value")
        val scope = ctx.attach()
        assertEquals(ctx, currentContext())
        assertEquals(currentContext(), currentContext())
        scope.detach()
    }

    @Test
    fun `contexts derived separately are not equal`() {
        assertNotEquals(rootContext().set(key, "value"), rootContext().set(key, "value"))
    }

    @Test
    fun `java adapter equality follows wrapped kotlin context`() {
        val ctx = rootContext().set(key, "value")
        assertEquals(OtelJavaContextAdapter(ctx), OtelJavaContextAdapter(ctx))
        assertEquals(OtelJavaContextAdapter(ctx).hashCode(), OtelJavaContextAdapter(ctx).hashCode())

        val javaKey = OtelJavaContextKey.named<String>("other")
        assertNotEquals(OtelJavaContextAdapter(ctx), OtelJavaContextAdapter(ctx).with(javaKey, "v"))
    }
}
