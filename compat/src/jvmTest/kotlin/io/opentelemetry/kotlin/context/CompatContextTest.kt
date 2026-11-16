package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.framework.SdkTracker
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class CompatContextTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `test root`() {
        assertSame(rootContext(), rootContext())
    }

    @Test
    fun `test store span`() {
        val tracer = createCompatOpenTelemetry().also(sdks::track).tracerProvider.getTracer("tracer")
        val span = tracer.startSpan("span")
        val ctx = rootContext().storeSpan(span)
        val retrievedSpan = ctx.extractSpan()
        assertSpanContextsMatch(span.spanContext, retrievedSpan.spanContext)
    }

    @Test
    fun `test same named keys are not interchangeable`() {
        val key = createContextKey<String>("my_key")
        val sameName = createContextKey<String>("my_key")
        val ctx = rootContext().set(key, "my_value")

        assertEquals("my_value", ctx.get(key))
        assertNull(ctx.get(sameName))
    }

    @Test
    fun `test current`() {
        createCompatOpenTelemetry().also(sdks::track)
        val ctx = rootContext().set(createContextKey("key"), "value")
        val scope = ctx.attach()
        try {
            assertSame(ctx, currentContext())
        } finally {
            scope.detach()
        }
    }
}
