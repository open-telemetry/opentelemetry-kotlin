package io.opentelemetry.kotlin.context

import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class JvmThreadLocalCurrentContextStorageTest {

    @Test
    fun testContextIsIsolatedPerThread() {
        val root = rootContext()
        val storage = ThreadLocalCurrentContextStorage()

        val mainContext = FakeContext()
        storage.attach(mainContext)

        val observedFromOtherThread = AtomicReference<Context>()
        val otherContext = FakeContext()

        val thread = Thread {
            observedFromOtherThread.set(storage.current())
            storage.attach(otherContext)
        }
        thread.start()
        thread.join()

        assertSame(root, observedFromOtherThread.get())
        assertNotSame(mainContext, observedFromOtherThread.get())
        assertSame(mainContext, storage.current())
    }
}
