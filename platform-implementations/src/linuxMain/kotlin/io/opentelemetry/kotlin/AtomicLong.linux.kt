package io.opentelemetry.kotlin

public actual class AtomicLong actual constructor(initialValue: Long) {
    private val impl = kotlin.concurrent.AtomicLong(initialValue)

    public actual fun get(): Long = impl.value
    public actual fun set(newValue: Long) {
        impl.value = newValue
    }

    public actual fun incrementAndGet(): Long = impl.addAndGet(1)
    public actual fun decrementAndGet(): Long = impl.addAndGet(-1)
    public actual fun addAndGet(delta: Long): Long = impl.addAndGet(delta)
    public actual fun getAndAdd(delta: Long): Long = impl.getAndAdd(delta)
    public actual fun compareAndSet(expect: Long, update: Long): Boolean = impl.compareAndSet(expect, update)
}
