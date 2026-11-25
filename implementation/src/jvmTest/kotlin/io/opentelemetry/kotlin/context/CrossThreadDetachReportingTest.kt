package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.tracing.FakeSpan
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CrossThreadDetachReportingTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    /**
     * The default storage is a single process-wide slot, so two threads that each attach and then
     * detach interleave as out-of-order detaches. That must not flood the error handler.
     */
    @Test
    fun testCrossThreadInterleaveReportsOutOfOrderOnce() {
        val errors = ConcurrentLinkedQueue<SdkError>()
        createOpenTelemetry { errorHandler(SdkErrorHandler(errors::add)) }.also(sdks::track)

        val barrier = CyclicBarrier(2)
        val failures = ConcurrentLinkedQueue<Throwable>()
        fun step() = barrier.await(5, TimeUnit.SECONDS)

        // Each iteration runs, in lockstep: first attaches, second attaches, first detaches
        // (out of order, as second's context is current), second detaches.
        val first = thread {
            runCatching {
                repeat(ITERATIONS) {
                    val scope = rootContext().storeSpan(FakeSpan()).attach()
                    step()
                    step()
                    scope.detach()
                    step()
                }
            }.onFailure(failures::add)
        }
        val second = thread {
            runCatching {
                repeat(ITERATIONS) {
                    step()
                    val scope = rootContext().storeSpan(FakeSpan()).attach()
                    step()
                    step()
                    scope.detach()
                }
            }.onFailure(failures::add)
        }
        first.join()
        second.join()

        assertTrue(failures.isEmpty(), "worker threads failed: $failures")
        val misuses = errors.filterIsInstance<SdkError.ApiMisuse>()
        assertEquals(1, misuses.size, "expected one report, got: ${misuses.map { it.message }}")
        assertEquals("Scope.detach", misuses.single().api)
    }

    private companion object {
        const val ITERATIONS = 1000
    }
}
