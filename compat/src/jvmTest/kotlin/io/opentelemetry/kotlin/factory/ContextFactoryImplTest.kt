package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.framework.SdkTracker
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class ContextFactoryImplTest {

    private val sdks = SdkTracker()

    private val contextFactory = CompatContextFactory()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `test root`() {
        assertSame(OtelJavaContext.root(), contextFactory.root().toOtelJavaContext())
    }

    @Test
    fun `test store span`() {
        val tracer = createCompatOpenTelemetry().also(sdks::track).tracerProvider.getTracer("tracer")
        val span = tracer.startSpan("span")
        val ctx = contextFactory.root().storeSpan(span)
        val retrievedSpan = ctx.extractSpan()
        assertSpanContextsMatch(span.spanContext, retrievedSpan.spanContext)
    }

    @Test
    fun `test same named keys are not interchangeable`() {
        val key = createContextKey<String>("my_key")
        val sameName = createContextKey<String>("my_key")
        val ctx = contextFactory.root().set(key, "my_value")

        assertEquals("my_value", ctx.get(key))
        assertNull(ctx.get(sameName))
    }

    @Test
    fun `test current`() {
        assertSame(OtelJavaContext.current(), contextFactory.current().toOtelJavaContext())
    }
}
