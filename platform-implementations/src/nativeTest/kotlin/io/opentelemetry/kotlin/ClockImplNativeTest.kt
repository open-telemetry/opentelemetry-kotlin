@file:OptIn(ExperimentalForeignApi::class)

package io.opentelemetry.kotlin

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.time
import kotlin.test.Test
import kotlin.test.assertTrue

internal class ClockImplNativeTest {
    @Test
    fun clockReturnsEpochNanoseconds() {
        val before = time(null)
        val nanos = getCurrentTimeNanos()
        val after = time(null)
        assertTrue(nanos >= before * 1_000_000_000L)
        assertTrue(nanos < (after + 1) * 1_000_000_000L)
    }
}
