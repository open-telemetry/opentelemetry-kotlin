package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.concurrent.Volatile

/**
 * Non-locking but thread-safe implementation of [ShutdownState]. Objects that can read but not modify
 * the shutdown state should use [ShutdownState] instead of this.
 */
@OptIn(ExperimentalApi::class)
public class MutableShutdownState : ShutdownState() {
    @Volatile
    override var isShutdown: Boolean = false
        private set

    public fun shutdown() {
        isShutdown = true
    }
}
