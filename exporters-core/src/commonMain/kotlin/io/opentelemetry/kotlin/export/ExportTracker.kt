package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ReentrantReadWriteLock
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

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
    // the Job key so export jobs are its children and still observe parent cancellation.
    private val scope =
        CoroutineScope(parentContext + SupervisorJob(parentContext[Job]))
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
        scope.coroutineContext.job.cancelChildren()
    }
}
