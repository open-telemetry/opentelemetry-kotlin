package io.opentelemetry.kotlin.error

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal actual object DeliveryReentrancyGuard {
    private var active = false

    actual fun isActive(): Boolean = active

    actual fun setActive(active: Boolean) {
        this.active = active
    }
}
