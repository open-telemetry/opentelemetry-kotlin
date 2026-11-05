@file:OptIn(ExperimentalForeignApi::class)

package io.opentelemetry.kotlin

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import platform.posix.setenv
import platform.posix.unsetenv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LinuxPlatformTest {
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
