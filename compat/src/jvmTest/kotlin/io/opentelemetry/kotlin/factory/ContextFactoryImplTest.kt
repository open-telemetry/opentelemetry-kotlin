package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class ContextFactoryImplTest {

    private val contextFactory = CompatContextFactory()

    @Test
    fun `test root`() {
        assertSame(OtelJavaContext.root(), contextFactory.root().toOtelJavaContext())
    }

    @Test
    fun `test store span`() {
        val tracer = createCompatOpenTelemetry().tracerProvider.getTracer("tracer")
        val span = tracer.startSpan("span")
        val ctx = contextFactory.root().storeSpan(span)
        val retrievedSpan = ctx.extractSpan()
        assertSpanContextsMatch(span.spanContext, retrievedSpan.spanContext)
    }

    @Test
    fun `test same named keys are not interchangeable`() {
        val key = contextFactory.createKey<String>("my_key")
        val sameName = contextFactory.createKey<String>("my_key")
        val ctx = contextFactory.root().set(key, "my_value")

        assertEquals("my_value", ctx.get(key))
        assertNull(ctx.get(sameName))
    }

    @Test
    fun `test current`() {
        assertSame(OtelJavaContext.current(), contextFactory.implicit().toOtelJavaContext())
    }
}
