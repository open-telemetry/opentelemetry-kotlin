package io.opentelemetry.kotlin

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

internal class ThreadSafeMapJvmTest {

    @Test
    fun `test threadSafeMap`() {
        assertTrue(threadSafeMap<String, String>() is ConcurrentHashMap)
    }
}
