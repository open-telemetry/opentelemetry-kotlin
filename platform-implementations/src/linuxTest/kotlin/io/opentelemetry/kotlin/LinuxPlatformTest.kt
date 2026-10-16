@file:OptIn(ExperimentalForeignApi::class)

package io.opentelemetry.kotlin

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import platform.posix.setenv
import platform.posix.time
import platform.posix.unsetenv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LinuxPlatformTest {
    @Test
    fun clockReturnsEpochNanoseconds() {
        val before = time(null)
        val nanos = getCurrentTimeNanos()
        val after = time(null)
        assertTrue(nanos >= before * 1_000_000_000L)
        assertTrue(nanos < (after + 1) * 1_000_000_000L)
    }

    @Test
    fun environmentVariablesAreReadable() {
        val name = "OTEL_KOTLIN_LINUX_PLATFORM_TEST"
        assertEquals(0, setenv(name, "value", 1))
        try {
            assertEquals("value", getEnvVarValue(name))
        } finally {
            unsetenv(name)
        }
        assertNull(getEnvVarValue(name))
    }

    @Test
    fun ioDispatcherRunsOffTheCallingThread() = runBlocking {
        val local = ThreadLocal<String>()
        local.set("caller")
        try {
            withContext(ioDispatcher) { assertNull(local.get()) }
        } finally {
            local.set(null)
        }
    }
}
