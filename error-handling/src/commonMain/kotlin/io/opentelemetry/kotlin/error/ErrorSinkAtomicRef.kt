package io.opentelemetry.kotlin.error

/**
 * Minimal atomic reference used by the process-wide error sink.
 */
internal expect class ErrorSinkAtomicRef<T>(initialValue: T) {
    fun get(): T
    fun set(value: T)
    fun compareAndSet(expect: T, update: T): Boolean
}
