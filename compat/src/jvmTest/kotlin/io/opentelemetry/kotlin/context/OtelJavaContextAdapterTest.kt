package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.factory.DefaultTraceFlagsFactory
import io.opentelemetry.kotlin.factory.DefaultTraceStateFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OtelJavaContextAdapterTest {

    private val contextFactory = CompatContextFactory()
    private val kotlinContextFactory = ContextFactoryImpl(SpanFactoryImpl(DefaultSpanContextFactory))
    private val kotlinSpan = NonRecordingSpan(
        DefaultSpanContextFactory.invalid,
        DefaultSpanContextFactory.create(
            traceId = "0af7651916cd43dd8448eb211c80319c",
            spanId = "b7ad6b7169203331",
            traceFlags = DefaultTraceFlagsFactory.default,
            traceState = DefaultTraceStateFactory.default,
            isRemote = false,
        ),
    )

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

    @Test
    fun `java reads span stored in kotlin context`() {
        val ctx = OtelJavaContextAdapter(kotlinContextFactory.root().storeSpan(kotlinSpan))
        val javaSpan = OtelJavaSpan.fromContext(ctx)
        assertEquals(kotlinSpan.spanContext.traceId, javaSpan.spanContext.traceId)
        assertEquals(kotlinSpan.spanContext.spanId, javaSpan.spanContext.spanId)
    }

    @Test
    fun `java reads no span from empty kotlin context`() {
        val ctx = OtelJavaContextAdapter(kotlinContextFactory.root())
        assertNull(OtelJavaSpan.fromContextOrNull(ctx))
        assertFalse(OtelJavaSpan.fromContext(ctx).spanContext.isValid)
    }

    @Test
    fun `kotlin reads span stored by java`() {
        val javaSpan = OtelJavaSpan.wrap(
            OtelJavaSpanContext.create(
                "0af7651916cd43dd8448eb211c80319c",
                "b7ad6b7169203331",
                OtelJavaTraceFlags.getSampled(),
                OtelJavaTraceState.getDefault(),
            )
        )
        val ctx = OtelJavaContextAdapter(kotlinContextFactory.root()).with(javaSpan)
        val span = ctx.toOtelKotlinContext().extractSpan()
        assertEquals(javaSpan.spanContext.traceId, span.spanContext.traceId)
        assertEquals(javaSpan.spanContext.spanId, span.spanContext.spanId)
    }

    @Test
    fun `kotlin span round trips through java`() {
        val ctx = OtelJavaContextAdapter(kotlinContextFactory.root()).with(OtelJavaSpanAdapter(kotlinSpan))
        assertSame(kotlinSpan, ctx.toOtelKotlinContext().extractSpan())
    }

    @Test
    fun `java reads baggage stored in kotlin context`() {
        val baggage = createBaggage { put("key", "value") }
        val ctx = OtelJavaContextAdapter(kotlinContextFactory.root().storeBaggage(baggage))
        assertEquals("value", OtelJavaBaggage.fromContext(ctx).getEntryValue("key"))
        assertTrue(OtelJavaBaggage.fromContext(OtelJavaContextAdapter(kotlinContextFactory.root())).isEmpty)
    }

    @Test
    fun `kotlin reads baggage stored by java`() {
        val baggage = OtelJavaBaggage.builder().put("key", "value").build()
        val ctx = baggage.storeInContext(OtelJavaContextAdapter(kotlinContextFactory.root()))
        assertEquals("value", ctx.toOtelKotlinContext().extractBaggage().getValue("key"))

        val cleared = ctx.with(OtelJavaBaggage.empty())
        assertTrue(cleared.toOtelKotlinContext().extractBaggage().asMap().isEmpty())
    }

    @Test
    fun `make current uses kotlin implicit context storage`() {
        val javaCurrent = OtelJavaContext.current()
        val previous = kotlinContextFactory.implicit()
        val kotlinCtx = kotlinContextFactory.root().storeSpan(kotlinSpan)

        OtelJavaContextAdapter(kotlinCtx).makeCurrent().use {
            assertSame(kotlinCtx, kotlinContextFactory.implicit())
            assertSame(javaCurrent, OtelJavaContext.current())
        }
        assertSame(previous, kotlinContextFactory.implicit())
    }

    @Test
    fun `wrapped runnable uses kotlin implicit context storage`() {
        val kotlinCtx = kotlinContextFactory.root().storeSpan(kotlinSpan)
        var observed: Context? = null
        OtelJavaContextAdapter(kotlinCtx).wrap(Runnable { observed = kotlinContextFactory.implicit() }).run()

        assertSame(kotlinCtx, observed)
        assertSame(kotlinContextFactory.root(), kotlinContextFactory.implicit())
    }
}
