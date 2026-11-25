package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ReentrantReadWriteLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Tracks export jobs launched by a simple processor so that [drain] can wait for them and
 * [close] can reject further admissions.
 *
 * Each admitted export is launched as a child of a dedicated [SupervisorJob] parented to the
 * processor scope, so exports still observe scope cancellation and are never run concurrently
 * with each other only if the caller serializes them (e.g. with a mutex inside [block]).
 *
 * Admission and closing are serialized by [lock]: [launch] registers the child job before
 * releasing the lock, and [close] sets [closed] under the same lock, so an export admitted
 * before shutdown can never escape [drain]'s snapshot of [Job.children].
 */
internal class ExportTracker(parentContext: CoroutineContext) {

    // Keeps the parent's dispatcher and other context elements; the SupervisorJob replaces
    // the Job key so export jobs are its children.
    private val scopeJob = SupervisorJob()
    private val scope = CoroutineScope(parentContext + scopeJob)

    init {
        // Propagate parent completion/cancellation without parenting scopeJob to the
        // parent's Job: a child Job would be reported as an uncompleted child coroutine
        // by runTest-style scopes that never complete until the test ends.
        parentContext[Job]?.invokeOnCompletion { scopeJob.cancel() }
    }
    private val lock = ReentrantReadWriteLock()
    private var closed = false

    /**
     * Launches [block] as a tracked export, or returns false once [close] has run.
     */
    fun launch(block: suspend CoroutineScope.() -> Unit): Boolean = lock.write {
        if (closed) {
            false
        } else {
            scope.launch(block = block)
            true
        }
    }

    /**
     * Suspends until every export admitted so far completes. Once [close] has run no new
     * children can appear, so the snapshot cannot miss an admitted export.
     */
    suspend fun drain() {
        scope.coroutineContext.job.children.toList().joinAll()
    }

    /**
     * Stops new admissions. Already admitted exports keep running so [drain] can wait for them.
     */
    fun close() {
        lock.write { closed = true }
    }

    /**
     * Cancels exports still running, e.g. after a drain timed out.
     */
    fun cancelRemaining() {
        scopeJob.cancelChildren()
    }

    /**
     * Cancels the tracker job itself along with any remaining export children. Call only
     * after [drain] has had its chance to wait for admitted exports; afterwards the tracker
     * must not be used again.
     */
    fun terminate() {
        close()
        scopeJob.cancel()
    }

    /** The job parenting all tracked export coroutines. Exposed for lifecycle tests. */
    internal val job: Job
        get() = scopeJob
}
