package io.opentelemetry.kotlin.context

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(TempContextStandInApi::class)
internal class SingleSlotCurrentContextStorageTest {

    private lateinit var root: Context
    private lateinit var storage: SingleSlotCurrentContextStorage

    @BeforeTest
    fun setUp() {
        root = internalRootContext()
        storage = SingleSlotCurrentContextStorage()
    }

    @Test
    fun testStorage() {
        assertSame(root, storage.current())
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
}
