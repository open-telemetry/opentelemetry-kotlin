package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ClockImplTest {

    @Test
    fun `anchored clock preserves sub-millisecond elapsed precision`() {
        val wallMillis = 1_700_000_000_000L
        var monoNanos = 1_000_000_000L
        val clock = AnchoredClock(
            wallClockMillis = { wallMillis },
            monoClockNanos = { monoNanos },
        )
        val anchor = clock.getNanos()
        assertEquals(wallMillis * 1_000_000L, anchor)

        // Advance monotonic time by a known sub-millisecond duration.
        monoNanos += 123_456L
        assertEquals(anchor + 123_456L, clock.getNanos())
    }

    @Test
    fun `anchored clock accepts timestamps exactly on a millisecond boundary`() {
        val wallMillis = 1_700_000_000_000L
        var monoNanos = 5_000_000_000L
        val clock = AnchoredClock(
            wallClockMillis = { wallMillis },
            monoClockNanos = { monoNanos },
        )

        // Exactly 1 ms of elapsed monotonic time lands on a millisecond boundary.
        monoNanos += 1_000_000L
        val timestamp = clock.getNanos()
        assertEquals(0L, timestamp % 1_000_000)
        assertEquals((wallMillis + 1) * 1_000_000L, timestamp)
    }

    @Test
    fun `anchored clock is stable when monotonic time does not advance`() {
        val wallMillis = 1_700_000_000_000L
        val monoNanos = 42L
        val clock = AnchoredClock(
            wallClockMillis = { wallMillis },
            monoClockNanos = { monoNanos },
        )

        val first = clock.getNanos()
        val second = clock.getNanos()
        assertEquals(first, second)
        assertEquals(wallMillis * 1_000_000L, first)
    }

    @Test
    fun `anchored clock ignores wall-clock changes after initialization`() {
        var wallMillis = 1_700_000_000_000L
        var monoNanos = 1_000_000_000L
        val clock = AnchoredClock(
            wallClockMillis = { wallMillis },
            monoClockNanos = { monoNanos },
        )

        monoNanos += 500_000_000L // 500 ms elapsed
        wallMillis += 60_000L // wall clock jumps forward a minute
        assertEquals(
            1_700_000_000_000L * 1_000_000L + 500_000_000L,
            clock.getNanos(),
        )
    }

    @Test
    fun `anchored clock refreshes anchor after overflow threshold`() {
        var wallMillis = 1_700_000_000_000L
        var monoNanos = 0L
        val clock = AnchoredClock(
            wallClockMillis = { wallMillis },
            monoClockNanos = { monoNanos },
        )

        // Simulate ~100 years of monotonic elapsed time to trigger the refresh.
        monoNanos = 3_155_760_000_000_000_001L
        wallMillis = 1_800_000_000_000L // wall clock has also advanced
        assertEquals(1_800_000_000_000L * 1_000_000L, clock.getNanos())

        // After refresh, elapsed resumes from the new anchor.
        monoNanos += 7L
        assertEquals(1_800_000_000_000L * 1_000_000L + 7L, clock.getNanos())
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
