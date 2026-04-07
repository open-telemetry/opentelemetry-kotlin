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
     * If not already shut down, call [shutdownNow] and run [action] within [timeoutMs] milliseconds.
     * Returns [OperationResultCode.Failure] if the timeout elapses before [action] completes.
     * If already shut down, returns [OperationResultCode.Success].
     */
    public suspend fun shutdown(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        action: suspend () -> OperationResultCode,
    ): OperationResultCode =
        if (isShutdown) {
            OperationResultCode.Success
        } else {
            shutdownNow()
            runWithTimeout(timeoutMs, action)
        }

    public companion object {
        public const val DEFAULT_TIMEOUT_MS: Long = 5000
    }
}
