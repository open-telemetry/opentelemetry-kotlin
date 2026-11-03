package io.opentelemetry.kotlin.error

/**
 * The JS runtime is single-threaded, so one shared flag is exact.
 */
internal actual object DeliveryReentrancyGuard {
    private var active = false

    actual fun isActive(): Boolean = active

    actual fun setActive(active: Boolean) {
        this.active = active
    }
}
