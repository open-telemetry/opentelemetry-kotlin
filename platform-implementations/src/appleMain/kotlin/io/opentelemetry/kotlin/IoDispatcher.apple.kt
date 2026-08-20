package io.opentelemetry.kotlin

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_queue_t
import kotlin.coroutines.CoroutineContext

/**
 * Dispatch work onto a global GCD queue.
 */
public actual val ioDispatcher: CoroutineDispatcher = GcdDispatcher

private object GcdDispatcher : CoroutineDispatcher() {

    @OptIn(ExperimentalForeignApi::class)
    private val queue: dispatch_queue_t =
        dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0uL)

    @OptIn(ExperimentalForeignApi::class)
    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        dispatch_async(queue) { block.run() }
    }
}
