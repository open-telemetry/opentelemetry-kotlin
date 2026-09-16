package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class ThreadLocalImplicitContextStorageTest {

    private lateinit var factory: ContextFactoryImpl
    private lateinit var storage: ImplicitContextStorage

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl(FakeSpanFactory())
        storage = ThreadLocalImplicitContextStorage(factory::root)
    }

    @Test
    fun testRootReturnedWhenNoContextSet() {
        assertSame(factory.root(), storage.implicitContext())
    }

    @Test
    fun testSetContextReturnedOnSameThread() {
        val other = FakeContext()
        storage.setImplicitContext(other)
        assertSame(other, storage.implicitContext())
    }

    @Test
    fun testSeparateInstancesAreIndependent() {
        val first = FakeContext()
        val second = FakeContext()
        val otherStorage = ThreadLocalImplicitContextStorage(factory::root)
        storage.setImplicitContext(first)
        otherStorage.setImplicitContext(second)
        assertSame(first, storage.implicitContext())
        assertSame(second, otherStorage.implicitContext())
        assertNotSame(storage.implicitContext(), otherStorage.implicitContext())
    }
}
