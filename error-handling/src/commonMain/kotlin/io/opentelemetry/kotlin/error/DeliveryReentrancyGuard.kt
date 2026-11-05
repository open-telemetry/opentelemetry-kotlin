package io.opentelemetry.kotlin.error

/**
 * Tracks, per thread, whether the current thread is inside a call to an [SdkErrorHandler]
 * delegate.
 */
internal expect object DeliveryReentrancyGuard {
    fun isActive(): Boolean
    fun setActive(active: Boolean)
}
