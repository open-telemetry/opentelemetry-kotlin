package io.opentelemetry.kotlin.error

import kotlin.concurrent.AtomicReference

internal actual class ErrorSinkAtomicRef<T> actual constructor(initialValue: T) {
    private val impl = AtomicReference(initialValue)

    actual fun get(): T = impl.value
    actual fun set(value: T) {
        impl.value = value
    }
    actual fun compareAndSet(expect: T, update: T): Boolean = impl.compareAndSet(expect, update)
}
