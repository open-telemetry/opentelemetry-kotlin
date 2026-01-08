package io.opentelemetry.kotlin

/**
 * A reentrant implementation of a read-write lock.
 */
public expect class ReentrantReadWriteLock() {

    /**
     * Performs an operation behind the write lock.
     */
    public inline fun <T> write(action: () -> T): T

    /**
     * Performs an operation behind the read lock.
     */
    public inline fun <T> read(action: () -> T): T
}
