package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.createNonRecordingSpan
import io.opentelemetry.kotlin.tracing.createSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class ContextValueTest {

    private val spanContext = createSpanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331") {
        isSampled = true
        isRemote = true
    }

    @Test
    fun testContextObtainRoot() {
        assertSame(rootContext(), rootContext())
    }

    @Test
    fun testContextCreateContextKey() {
        assertNotEquals(createContextKey<String>("my_key"), createContextKey("my_key"))
    }

    @Test
    fun testContextSameNamedKeysAreNotInterchangeable() {
        val key = createContextKey<String>("my_key")
        val sameName = createContextKey<String>("my_key")
        val ctx = rootContext().set(key, "my_value")
        assertEquals("my_value", ctx.get(key))
        assertNull(ctx.get(sameName))
    }

    @Test
    fun testContextGetAbsentValue() {
        val key = createContextKey<String>("my_key")
        assertNull(rootContext().get(key))
    }

    @Test
    fun testContextGetPresentValue() {
        val ctx = rootContext()
        val key = createContextKey<String>("my_key")
        val value = "my_value"
        val newCtx = ctx.set(key, value)
        assertNull(ctx.get(key))
        assertEquals(value, newCtx.get(key))
    }

    @Test
    fun testContextMultipleValues() {
        val key1 = createContextKey<String>("my_key1")
        val key2 = createContextKey<String>("my_key2")
        val key3 = createContextKey<Int>("my_key3")
        val value1 = "my_value1"
        val value2 = "my_value2"
        val value3 = 42
        val newCtx = rootContext().set(key1, value1).set(key2, value2).set(key3, value3)

        assertEquals(value1, newCtx.get(key1))
        assertEquals(value2, newCtx.get(key2))
        assertEquals(value3, newCtx.get(key3))
    }

    @Test
    fun testContextOverrideExistingKey() {
        val key = createContextKey<String>("my_key")
        val newCtx = rootContext().set(key, "my_value1").set(key, "my_value2")
        assertEquals("my_value2", newCtx.get(key))
    }

    @Test
    fun testContextKeyExplicitNull() {
        val key = createContextKey<String>("key")
        val newCtx = rootContext().set(key, "value").set(key, null)
        assertNull(newCtx.get(key))
    }

    @Test
    fun testAttachDetachIsNoOp() {
        val ctx = rootContext().set(createContextKey("key"), "value")
        assertTrue(ctx.attach().detach())
        assertTrue(rootContext().attach().detach())
    }

    @Test
    fun testExtractSpanFromRootReturnsInvalid() {
        val span = rootContext().extractSpan()
        assertEquals(createInvalidSpanContext(), span.spanContext)
        assertFalse(span.isRecording())
    }

    @Test
    fun testStoreSpanRoundTrip() {
        val span = createNonRecordingSpan(spanContext)
        val ctx = rootContext().storeSpan(span)
        assertSame(span, ctx.extractSpan())
    }

    @Test
    fun testStoreSpanReturnsNewContext() {
        val root = rootContext()
        val derived = root.storeSpan(createNonRecordingSpan(spanContext))
        assertNotSame(root, derived)
        assertEquals(createInvalidSpanContext(), root.extractSpan().spanContext)
    }

    @Test
    fun testStoreSpanContextRoundTrip() {
        val root = rootContext()
        val ctx = root.storeSpanContext(spanContext)

        assertNotSame(root, ctx)
        assertSame(spanContext, ctx.extractSpan().spanContext)
        assertTrue(ctx.extractSpan().spanContext.isRemote)
        assertFalse(root.extractSpan().spanContext.isValid)
    }

    @Test
    fun testStoreSpanContextInvalidStoresInvalidSpan() {
        val ctx = rootContext()
            .storeSpan(createNonRecordingSpan(spanContext))
            .storeSpanContext(createInvalidSpanContext())
        assertFalse(ctx.extractSpan().spanContext.isValid)
    }

    @Test
    fun testExtractBaggageFromRootReturnsEmpty() {
        assertTrue(rootContext().extractBaggage().asMap().isEmpty())
    }

    @Test
    fun testStoreBaggageRoundTrip() {
        val baggage = createBaggage { put("key", "value") }
        val root = rootContext()
        val ctx = root.storeBaggage(baggage)

        assertNotSame(root, ctx)
        assertSame(baggage, ctx.extractBaggage())
        assertTrue(root.extractBaggage().asMap().isEmpty())
    }

    @Test
    fun testClearBaggage() {
        val baggage = createBaggage { put("key", "value") }
        val ctx = rootContext().storeBaggage(baggage).clearBaggage()
        assertTrue(ctx.extractBaggage().asMap().isEmpty())
    }
}
