package io.opentelemetry.kotlin

import kotlin.native.concurrent.ThreadLocal

private val instanceCounter = AtomicLong()

@ThreadLocal
private val threadValues = mutableMapOf<Long, Any>()

/**
 * Native implementation of thread-local storage, backed by a map annotated with
 * [kotlin.native.concurrent.ThreadLocal] so each thread sees its own copy. Every instance uses a
 * unique key so separate instances stay independent.
 */
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
