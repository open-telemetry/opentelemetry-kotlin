package io.opentelemetry.kotlin

public actual class AtomicBoolean actual constructor(initialValue: Boolean) {
    private val impl = AtomicLong(initialValue.toAtomicValue())

    public actual fun get(): Boolean = impl.get() != 0L
    public actual fun set(newValue: Boolean) {
        impl.set(newValue.toAtomicValue())
    }

    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean =
        impl.compareAndSet(expect.toAtomicValue(), update.toAtomicValue())
}

private fun Boolean.toAtomicValue(): Long = if (this) {
    1L
} else {
    0L
}
