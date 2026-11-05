package io.opentelemetry.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import platform.Foundation.NSThread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame

@Suppress("InjectDispatcher")
internal class IoDispatcherTest {

    @Test
    fun testWorkRunsOffTheMainThread() = runBlocking {
        val onMainThread = withContext(ioDispatcher) {
            NSThread.isMainThread()
        }
        assertFalse(onMainThread)
    }

    @Test
    fun testDispatcherExecutesWork() = runBlocking {
        val result = withContext(ioDispatcher) { "dispatched" }
        assertEquals("dispatched", result)
    }

    @Test
    fun testDispatcherIsNotDefault() {
        assertNotSame(Dispatchers.Default, ioDispatcher)
    }
}
