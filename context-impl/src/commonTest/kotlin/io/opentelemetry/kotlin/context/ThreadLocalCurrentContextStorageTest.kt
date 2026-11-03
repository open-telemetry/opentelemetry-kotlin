package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class ThreadLocalCurrentContextStorageTest {

    private lateinit var factory: ContextFactoryImpl
    private lateinit var storage: CurrentContextStorage

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl()
        storage = ThreadLocalCurrentContextStorage(factory::root)
    }

    @Test
    fun testRootReturnedWhenNoContextSet() {
        assertSame(factory.root(), storage.current())
    }

    @Test
    fun testSetContextReturnedOnSameThread() {
        val other = FakeContext()
        storage.setCurrent(other)
        assertSame(other, storage.current())
    }

    @Test
    fun testSeparateInstancesAreIndependent() {
        val first = FakeContext()
        val second = FakeContext()
        val otherStorage = ThreadLocalCurrentContextStorage(factory::root)
        storage.setCurrent(first)
        otherStorage.setCurrent(second)
        assertSame(first, storage.current())
        assertSame(second, otherStorage.current())
        assertNotSame(storage.current(), otherStorage.current())
    }
}
