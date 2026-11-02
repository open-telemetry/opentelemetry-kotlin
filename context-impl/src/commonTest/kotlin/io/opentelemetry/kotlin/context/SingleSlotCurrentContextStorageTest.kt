package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

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
        storage.setCurrent(other)
        assertSame(other, storage.current())
    }
}
