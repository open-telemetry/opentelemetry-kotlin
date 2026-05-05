package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

internal class DefaultImplicitContextStorageImplTest {

    private lateinit var factory: ContextFactoryImpl
    private lateinit var storage: DefaultImplicitContextStorage

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl(SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl())))
        storage = DefaultImplicitContextStorage(factory::root)
    }

    @Test
    fun testStorage() {
        assertSame(factory.root(), storage.implicitContext())
        val other = FakeContext()
        storage.setImplicitContext(other)
        assertSame(other, storage.implicitContext())
    }
}
