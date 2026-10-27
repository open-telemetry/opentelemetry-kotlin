package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class JvmThreadLocalCurrentContextStorageTest {

    @Test
    fun testContextIsIsolatedPerThread() {
        val factory = ContextFactoryImpl()
        val storage = ThreadLocalCurrentContextStorage(factory::root)

        val mainContext = FakeContext()
        storage.setCurrent(mainContext)

        val observedFromOtherThread = AtomicReference<Context>()
        val otherContext = FakeContext()

        val thread = Thread {
            observedFromOtherThread.set(storage.current())
            storage.setCurrent(otherContext)
        }
        thread.start()
        thread.join()

        assertSame(factory.root(), observedFromOtherThread.get())
        assertNotSame(mainContext, observedFromOtherThread.get())
        assertSame(mainContext, storage.current())
    }
}
