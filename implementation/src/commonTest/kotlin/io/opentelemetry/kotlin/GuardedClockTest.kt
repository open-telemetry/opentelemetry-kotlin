package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class GuardedClockTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun testDelegatesOrFallsBack() {
        assertEquals(5, GuardedClock(FakeClock(5), errorHandler, FakeClock(10)).now())
        assertEquals(10, GuardedClock({ error("boom") }, errorHandler, FakeClock(10)).now())

        val error = errorHandler.userCodeErrors.single()
        assertEquals("Clock.now failed, using fallback clock", error.message)
        assertEquals("boom", error.cause.message)
    }

    @Test
    fun testGuardedSkipsSdkClocks() {
        val sdkClock = ClockImpl()
        assertSame(sdkClock, sdkClock.guarded(errorHandler))
        val guarded = assertIs<GuardedClock>(FakeClock().guarded(errorHandler))
        assertSame(guarded, guarded.guarded(errorHandler))
    }
}
