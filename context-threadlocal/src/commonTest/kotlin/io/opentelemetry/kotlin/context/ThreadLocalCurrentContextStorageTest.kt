package io.opentelemetry.kotlin.context

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(TempContextStandInApi::class)
internal class ThreadLocalCurrentContextStorageTest {

    private lateinit var root: Context
    private lateinit var storage: CurrentContextStorage

    @BeforeTest
    fun setUp() {
        root = internalRootContext()
        storage = createThreadLocalCurrentContextStorage()
    }

    @Test
    fun testRootReturnedWhenNoContextSet() {
        assertSame(root, storage.current())
    }

    @Test
    fun testSetContextReturnedOnSameThread() {
        val other = FakeContext()
        storage.attach(other)
        assertSame(other, storage.current())
    }

    @Test
    fun testDetachRestoresPreviousState() {
        val outer = FakeContext()
        val inner = FakeContext()
        val outerScope = storage.attach(outer)
        val innerScope = storage.attach(inner)
        assertSame(inner, storage.current())

        assertTrue(innerScope.detach())
        assertSame(outer, storage.current())
        assertTrue(outerScope.detach())
        assertSame(root, storage.current())
    }

    @Test
    fun testSeparateInstancesAreIndependent() {
        val first = FakeContext()
        val second = FakeContext()
        val otherStorage = createThreadLocalCurrentContextStorage()
        storage.attach(first)
        assertSame(root, otherStorage.current())
        otherStorage.attach(second)
        assertSame(first, storage.current())
        assertSame(second, otherStorage.current())
        assertNotSame(storage.current(), otherStorage.current())
    }
}
