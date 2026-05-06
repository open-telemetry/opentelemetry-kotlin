package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class JvmThreadLocalImplicitContextStorageTest {

    @Test
    fun testContextIsIsolatedPerThread() {
        val factory = ContextFactoryImpl(SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl())))
        val storage = threadLocalImplicitContextStorage(factory::root)

        val mainContext = FakeContext()
        storage.setImplicitContext(mainContext)

        val observedFromOtherThread = AtomicReference<Context>()
        val otherContext = FakeContext()

        val thread = Thread {
            observedFromOtherThread.set(storage.implicitContext())
            storage.setImplicitContext(otherContext)
        }
        thread.start()
        thread.join()

        assertSame(factory.root(), observedFromOtherThread.get())
        assertNotSame(mainContext, observedFromOtherThread.get())
        assertSame(mainContext, storage.implicitContext())
    }
}
