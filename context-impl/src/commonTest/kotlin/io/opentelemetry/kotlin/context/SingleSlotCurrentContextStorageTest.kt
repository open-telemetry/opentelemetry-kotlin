package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SingleSlotCurrentContextStorageTest {

    private lateinit var factory: ContextFactoryImpl
    private lateinit var storage: SingleSlotCurrentContextStorage

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl()
        storage = SingleSlotCurrentContextStorage(factory::root)
    }

    @Test
    fun testStorage() {
        assertSame(factory.root(), storage.current())
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
        assertSame(factory.root(), storage.current())
    }
}
