package io.opentelemetry.kotlin.export

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ExportTrackerTest {

    @Test
    fun testDrainWaitsForAdmittedExports() = runTest {
        val tracker = ExportTracker(coroutineContext)
        val gate = CompletableDeferred<Unit>()
        var exported = false

        tracker.launch {
            gate.await()
            exported = true
        }
        tracker.close()

        val drain = async { tracker.drain() }
        advanceUntilIdle()
        assertFalse(exported)
        assertFalse(drain.isCompleted)

        gate.complete(Unit)
        drain.await()
        assertTrue(exported)
    }

    @Test
    fun testLaunchRejectedAfterClose() = runTest {
        val tracker = ExportTracker(coroutineContext)
        tracker.close()
        assertFalse(tracker.launch { })
    }

    @Test
    fun testTerminateCancelsJobAndRemainingExports() = runTest {
        val tracker = ExportTracker(coroutineContext)
        val gate = CompletableDeferred<Unit>()
        var finished = false

        tracker.launch {
            gate.await()
            finished = true
        }

        tracker.terminate()
        advanceUntilIdle()

        assertFalse(finished)
        assertTrue(tracker.job.isCancelled || tracker.job.isCompleted)
        // The tracker cannot be reused after termination.
        assertFalse(tracker.launch { })
    }

    @Test
    fun testParentJobCompletionStopsExports() = runTest {
        val parent = Job(coroutineContext[Job])
        val tracker = ExportTracker(coroutineContext + parent)
        val gate = CompletableDeferred<Unit>()
        var finished = false

        tracker.launch {
            gate.await()
            finished = true
        }

        // Cancelling the parent context's job must cancel the tracker job and its
        // export children through invokeOnCompletion propagation.
        parent.cancel()
        advanceUntilIdle()

        assertFalse(finished)
        assertTrue(tracker.job.isCancelled || tracker.job.isCompleted)
    }
}
