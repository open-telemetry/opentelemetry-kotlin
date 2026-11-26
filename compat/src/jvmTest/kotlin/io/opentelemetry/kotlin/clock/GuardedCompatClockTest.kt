package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GuardedCompatClockTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `delegates without reading the fallback`() {
        val fallback = CountingClock(10)
        val clock = GuardedCompatClock(FakeClock(5), errorHandler, fallback)

        assertEquals(5, clock.now())
        assertEquals(0, fallback.reads)
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun `falls back and reports when the delegate throws`() {
        val fallback = CountingClock(10)
        val clock = GuardedCompatClock({ error("boom") }, errorHandler, fallback)

        assertEquals(10, clock.now())
        assertEquals(1, fallback.reads)
        val error = errorHandler.userCodeErrors.single()
        assertEquals("Clock.now failed, using fallback clock. Later failures are not reported.", error.message)
        assertEquals("boom", error.cause.message)
    }

    @Test
    fun `repeated failures are reported once`() {
        val fallback = CountingClock(10)
        val clock = GuardedCompatClock({ error("boom") }, errorHandler, fallback)

        repeat(1000) { assertEquals(10, clock.now()) }
        assertEquals(1000, fallback.reads)
        assertEquals(1, errorHandler.userCodeErrors.size)
    }

    @Test
    fun `guarded skips sdk clocks`() {
        val sdkClock = ClockAdapter()
        assertSame(sdkClock, sdkClock.guarded(errorHandler))
        val guarded = assertIs<GuardedCompatClock>(FakeClock().guarded(errorHandler))
        assertSame(guarded, guarded.guarded(errorHandler))
    }

    private class CountingClock(private val time: Long) : Clock {
        var reads = 0
        override fun now(): Long {
            reads++
            return time
        }
    }
}
