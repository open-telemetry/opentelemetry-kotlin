package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ReportOnceTest {

    @Test
    fun testAcquiresOnlyOnFirstCall() {
        val reportOnce = ReportOnce()
        assertEquals(1, (1..1000).count { reportOnce.tryAcquire() })
    }

    @Test
    fun testInstancesAreIndependent() {
        assertTrue(ReportOnce().tryAcquire())
        assertTrue(ReportOnce().tryAcquire())
    }
}
