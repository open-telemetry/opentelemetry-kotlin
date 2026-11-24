package io.opentelemetry.kotlin.error

import java.util.Collections
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DefaultErrorLogLimiterJvmTest {
    @Test
    fun concurrentReportsDoNotLoseCountsOrDuplicateFirstLog() {
        val limiter = DefaultErrorLogLimiter()
        val cause = IllegalStateException("shared")
        val error = SdkError.UserCodeError(cause, "export", SdkErrorSeverity.WARNING)
        val decisions = Collections.synchronizedList(mutableListOf<DefaultErrorLogLimiter.Decision>())
        val n = 8
        val start = CountDownLatch(1)
        val threads = (1..n).map {
            thread {
                start.await()
                repeat(1000) { decisions.add(limiter.record(error)) }
            }
        }
        start.countDown()
        threads.forEach { it.join() }
        assertEquals(8000, decisions.size)
        assertEquals(1, decisions.count { it == DefaultErrorLogLimiter.Decision.Full })
        val summaryCounts = decisions.filterIsInstance<DefaultErrorLogLimiter.Decision.Repeated>()
            .map { it.count }.sorted()
        assertEquals((0..12).map { 1L shl it }, summaryCounts)
    }
}
