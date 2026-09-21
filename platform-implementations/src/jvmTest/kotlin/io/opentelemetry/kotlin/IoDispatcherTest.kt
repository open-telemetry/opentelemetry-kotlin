package io.opentelemetry.kotlin

import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@Suppress("InjectDispatcher")
internal class IoDispatcherTest {

    @Test
    fun testDispatcherIsIo() {
        assertSame(Dispatchers.IO, ioDispatcher)
    }

    @Test
    fun testDispatcherIsNotDefault() {
        assertNotSame(Dispatchers.Default, ioDispatcher)
    }
}
