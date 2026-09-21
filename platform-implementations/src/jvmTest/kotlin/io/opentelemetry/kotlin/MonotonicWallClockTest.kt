package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MonotonicWallClockTest {

    @Test
    fun `now follows elapsed realtime from the initial wall clock`() {
        var elapsedNanos = 5_000L
        val clock = MonotonicWallClock(
            wallTimeMillis = { 1_700_000_000_000L },
            elapsedRealtimeNanos = { elapsedNanos },
        )

        val start = clock.now()
        assertEquals(1_700_000_000_000L * 1_000_000L, start)

        elapsedNanos += 250L
        assertEquals(start + 250L, clock.now())
    }

    @Test
    fun `wall clock adjustments after the baseline are ignored`() {
        var wallMillis = 1_700_000_000_000L
        val clock = MonotonicWallClock(
            wallTimeMillis = { wallMillis },
            elapsedRealtimeNanos = { 10L },
        )
        val start = clock.now()

        wallMillis += 60_000L
        assertEquals(start, clock.now())

        wallMillis -= 120_000L
        assertEquals(start, clock.now())
    }

    @Test
    fun `now does not move backwards when the wall clock does`() {
        var wallMillis = 1_700_000_000_000L
        var elapsedNanos = 0L
        val clock = MonotonicWallClock(
            wallTimeMillis = { wallMillis },
            elapsedRealtimeNanos = { elapsedNanos },
        )
        val start = clock.now()

        wallMillis -= 60_000L
        elapsedNanos += 1_000L

        val current = clock.now()
        assertTrue(current >= start)
        assertEquals(start + 1_000L, current)
    }
}
