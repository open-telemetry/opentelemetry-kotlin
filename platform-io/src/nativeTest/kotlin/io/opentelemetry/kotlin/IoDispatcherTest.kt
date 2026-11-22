package io.opentelemetry.kotlin

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import platform.posix.pthread_self
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

@OptIn(ExperimentalForeignApi::class)
@Suppress("InjectDispatcher")
internal class IoDispatcherTest {

    @Test
    fun testDispatcherIsIo() {
        assertSame(Dispatchers.IO, ioDispatcher)
    }

    @Test
    fun testWorkRunsOffTheCallingThread() = runBlocking {
        val caller = pthread_self()
        val worker = withContext(ioDispatcher) { pthread_self() }
        assertNotEquals(caller, worker)
    }
}
