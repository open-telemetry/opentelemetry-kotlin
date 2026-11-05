package io.opentelemetry.kotlin.error

internal actual class ErrorSinkAtomicRef<T> actual constructor(initialValue: T) {
    private var value: T = initialValue

    actual fun get(): T = value

    actual fun set(value: T) {
        this.value = value
    }

    actual fun compareAndSet(expect: T, update: T): Boolean {
        if (value !== expect) {
            return false
        }
        value = update
        return true
    }
}
