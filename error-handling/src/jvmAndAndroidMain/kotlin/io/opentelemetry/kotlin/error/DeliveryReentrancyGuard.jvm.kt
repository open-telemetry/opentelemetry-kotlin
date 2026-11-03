package io.opentelemetry.kotlin.error

internal actual object DeliveryReentrancyGuard {
    private val active = ThreadLocal<Boolean>()

    actual fun isActive(): Boolean = active.get() == true

    actual fun setActive(active: Boolean) {
        if (active) {
            this.active.set(true)
        } else {
            this.active.remove()
        }
    }
}
