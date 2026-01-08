package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertTrue

internal class ClockImplJvmTest {

    @Test
    fun `getCurrentTimeNanos returns consistent nanosecond format`() {
        val timestamp = getCurrentTimeNanos()

        // Should end with 6 zeros since we convert milliseconds to nanoseconds
        // (milliseconds * 1_000_000 always ends in 6 zeros)
        val lastSixDigits = timestamp % 1_000_000
        assertTrue(lastSixDigits == 0L)
    }

    @Test
    fun `getCurrentTimeNanos should handle multiple rapid calls`() {
        val iterations = 100
        var previousTime = 0L

        repeat(iterations) {
            val currentTime = getCurrentTimeNanos()
            assertTrue(currentTime >= previousTime)
            previousTime = currentTime
        }
    }

    @Test
    fun `getCurrentTimeNanos returns reasonable current time`() {
        val timestamp = getCurrentTimeNanos()
        // Convert to seconds for comparison
        val timestampInSeconds = timestamp / 1_000_000_000L

        // Should be sometime after 2020 (timestamp > Jan 1, 2020)
        val jan2020 = 1577836800L // Jan 1, 2020 in seconds
        assertTrue(timestampInSeconds > jan2020)

        // Should be before year 2100 (timestamp < Jan 1, 2100)
        val jan2100 = 4102444800L // Jan 1, 2100 in seconds
        assertTrue(timestampInSeconds < jan2100)
    }
}
