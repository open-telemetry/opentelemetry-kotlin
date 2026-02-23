package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import kotlin.concurrent.Volatile

/**
 * Non-locking but thread-safe implementation of [ShutdownState]. Objects that can read but not modify
 * the shutdown state should use [ShutdownState] instead of this.
 */
@ThreadSafe
@ExperimentalApi
public class MutableShutdownState : ShutdownState() {
    @Volatile
    override var isShutdown: Boolean = false
        private set

    /**
     * Perform shutdown upon invocation
     */
    public fun shutdownNow() {
        isShutdown = true
    }

    /**
     * If not already shut down, call [shutdownNow] and run [action] to perform additional cleanup.
     * [action] must be thread-safe and be able to be invoked many times without issue.
     * If already shut down, return [OperationResultCode.Success].
     * This method will not handle exceptions thrown by [action].
     */
    public inline fun shutdown(
        action: () -> OperationResultCode,
    ): OperationResultCode =
        if (isShutdown) {
            OperationResultCode.Success
        } else {
            shutdownNow()
            action()
        }
}
