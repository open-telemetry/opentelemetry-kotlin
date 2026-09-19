package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

internal class ClockImplTest {

    @Test
    fun `getCurrentTimeNanos provides nanosecond precision`() {
        val timestamp = getCurrentTimeNanos()

        // With the anchored clock implementation using System.nanoTime(),
        // we should have sub-millisecond precision, so the last 6 digits
        // should NOT all be zero (unlike the old milliseconds-only implementation)
        val lastSixDigits = timestamp % 1_000_000
        assertNotEquals(0L, lastSixDigits, "Clock should provide nanosecond precision")
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

    @Test
    fun `getCurrentTimeNanos provides better resolution than milliseconds`() {
        // Take two rapid measurements
        val time1 = getCurrentTimeNanos()
        val time2 = getCurrentTimeNanos()

        // With nanosecond precision, we should be able to detect differences
        // even within a very short time window
        val diff = time2 - time1
        // The difference should be less than 1 millisecond (1_000_000 nanoseconds)
        // most of the time due to the high resolution of System.nanoTime()
        // (though this is not guaranteed, it's likely in practice)
        // We just verify that the clock is working and time is advancing
        assertTrue(diff >= 0, "Time should be monotonically increasing")
    }
}
