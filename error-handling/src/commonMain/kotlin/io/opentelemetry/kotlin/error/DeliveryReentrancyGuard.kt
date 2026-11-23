package io.opentelemetry.kotlin.error

import io.opentelemetry.kotlin.ThreadLocal

/**
 * Tracks, per thread, whether the current thread is inside a call to an [SdkErrorHandler]
 * delegate.
 */
internal object DeliveryReentrancyGuard {
    private val active = ThreadLocal<Boolean>()

    fun isActive(): Boolean = active.get() == true

    fun setActive(active: Boolean) {
        this.active.set(active.takeIf { it })
    }
}
