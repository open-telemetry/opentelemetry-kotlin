package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.createSpanContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ContextImplTest {

    private lateinit var factory: ContextFactoryImpl

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl()
    }

    @Test
    fun testContextObtainRoot() {
        assertSame(factory.root(), factory.root())
    }

    @Test
    fun testContextCreateContextKey() {
        assertNotEquals(createContextKey<String>("my_key"), createContextKey("my_key"))
    }

    @Test
    fun testContextSameNamedKeysAreNotInterchangeable() {
        val key = createContextKey<String>("my_key")
        val sameName = createContextKey<String>("my_key")
        val ctx = factory.root().set(key, "my_value")

        assertEquals("my_value", ctx.get(key))
        assertNull(ctx.get(sameName))
    }

    @Test
    fun testContextGetAbsentValue() {
        val ctx = factory.root()
        val key = createContextKey<String>("my_key")
        assertNull(ctx.get(key))
    }

    @Test
    fun testContextGetPresentValue() {
        val ctx = factory.root()
        val key = createContextKey<String>("my_key")
        val value = "my_value"
        val newCtx = ctx.set(key, value)

        assertNull(ctx.get(key))
        assertEquals(value, newCtx.get(key))
    }

    @Test
    fun testContextMultipleValues() {
        val ctx = factory.root()
        val key1 = createContextKey<String>("my_key1")
        val key2 = createContextKey<String>("my_key2")
        val key3 = createContextKey<Int>("my_key3")
        val value1 = "my_value1"
        val value2 = "my_value2"
        val value3 = 42
        val newCtx = ctx.set(key1, value1).set(key2, value2).set(key3, value3)

        assertEquals(value1, newCtx.get(key1))
        assertEquals(value2, newCtx.get(key2))
        assertEquals(value3, newCtx.get(key3))
    }

    @Test
    fun testContextOverrideExistingKey() {
        val ctx = factory.root()
        val key = createContextKey<String>("my_key")
        val value1 = "my_value1"
        val value2 = "my_value2"

        val newCtx = ctx.set(key, value1).set(key, value2)
        assertEquals(value2, newCtx.get(key))
    }

    @Test
    fun testContextKeyExplicitNull() {
        val ctx = factory.root()
        val key = createContextKey<String>("key")
        val newCtx = ctx.set(key, null)
        assertNull(newCtx.get(key))
    }

    @Test
    fun testAttach() {
        val ctx = factory.root()
        ctx.attach()
        assertSame(ctx, factory.current())
    }

    @Test
    fun testImplicitContext() {
        val ctx = factory.root()
        assertSame(ctx, factory.current())
    }

    @Test
    fun testExtractSpanFromRootReturnsInvalid() {
        assertEquals(createInvalidSpanContext(), factory.root().extractSpan().spanContext)
    }

    @Test
    fun testStoreSpanRoundTrip() {
        val span = FakeSpan()
        val ctx = factory.root().storeSpan(span)
        assertSame(span, ctx.extractSpan())
    }

    @Test
    fun testStoreSpanReturnsNewContext() {
        val span = FakeSpan()
        val root = factory.root()
        val derived = root.storeSpan(span)
        assertNotSame(root, derived)
        assertEquals(createInvalidSpanContext(), root.extractSpan().spanContext)
    }

    @Test
    fun testStoreSpanContextRoundTrip() {
        val ctxFactory = ContextFactoryImpl()
        val spanContext = createSpanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331") {
            isSampled = true
            isRemote = true
        }
        val root = ctxFactory.root()
        val ctx = root.storeSpanContext(spanContext)

        assertNotSame(root, ctx)
        assertSame(spanContext, ctx.extractSpan().spanContext)
        assertTrue(ctx.extractSpan().spanContext.isRemote)
        assertFalse(root.extractSpan().spanContext.isValid)
    }

    @Test
    fun testStoreSpanContextInvalidStoresInvalidSpan() {
        val ctxFactory = ContextFactoryImpl()
        val span = FakeSpan()
        val ctx = ctxFactory.root().storeSpan(span).storeSpanContext(createInvalidSpanContext())
        assertFalse(ctx.extractSpan().spanContext.isValid)
    }
}
