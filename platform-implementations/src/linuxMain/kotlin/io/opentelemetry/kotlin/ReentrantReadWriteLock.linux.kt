package io.opentelemetry.kotlin

import kotlinx.atomicfu.locks.reentrantLock

public actual class ReentrantReadWriteLock {
    private val impl = reentrantLock()

    @PublishedApi
    internal fun lock() {
        impl.lock()
    }

    @PublishedApi
    internal fun unlock() {
        impl.unlock()
    }

    public actual inline fun <T> write(action: () -> T): T {
        lock()
        try {
            return action()
        } finally {
            unlock()
        }
    }

    public actual inline fun <T> read(action: () -> T): T = write(action)
}
