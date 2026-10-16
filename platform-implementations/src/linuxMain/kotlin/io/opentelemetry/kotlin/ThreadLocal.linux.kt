package io.opentelemetry.kotlin

import kotlin.native.concurrent.ThreadLocal

private val instanceCounter = AtomicLong()

@ThreadLocal
private val threadValues = mutableMapOf<Long, Any>()

public actual class ThreadLocal<T> actual constructor() {
    private val key = instanceCounter.incrementAndGet()

    @Suppress("UNCHECKED_CAST")
    public actual fun get(): T? = threadValues[key] as T?

    public actual fun set(value: T?) {
        if (value == null) {
            threadValues.remove(key)
        } else {
            threadValues[key] = value
        }
    }
}
