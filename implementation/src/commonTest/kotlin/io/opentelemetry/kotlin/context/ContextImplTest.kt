package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
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
        val ctx = factory.root()
        assertNotEquals(ctx.createKey<String>("my_key"), ctx.createKey("my_key"))
    }

    @Test
    fun testContextGetAbsentValue() {
        val ctx = factory.root()
        val key = ctx.createKey<String>("my_key")
        assertNull(ctx.get(key))
    }

    @Test
    fun testContextGetPresentValue() {
        val ctx = factory.root()
        val key = ctx.createKey<String>("my_key")
        val value = "my_value"
        val newCtx = ctx.set(key, value)

        assertNull(ctx.get(key))
        assertEquals(value, newCtx.get(key))
    }

    @Test
    fun testContextMultipleValues() {
        val ctx = factory.root()
        val key1 = ctx.createKey<String>("my_key1")
        val key2 = ctx.createKey<String>("my_key2")
        val key3 = ctx.createKey<Int>("my_key3")
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
        val key = ctx.createKey<String>("my_key")
        val value1 = "my_value1"
        val value2 = "my_value2"

        val newCtx = ctx.set(key, value1).set(key, value2)
        assertEquals(value2, newCtx.get(key))
    }

    @Test
    fun testContextKeyExplicitNull() {
        val ctx = factory.root()
        val key = ctx.createKey<String>("key")
        val newCtx = ctx.set(key, null)
        assertNull(newCtx.get(key))
    }

    @Test
    fun testAttach() {
        val ctx = factory.root()
        ctx.attach()
        assertSame(ctx, factory.implicitContext())
    }

    @Test
    fun testImplicitContext() {
        val ctx = factory.root()
        assertSame(ctx, factory.implicitContext())
    }
}
