package io.opentelemetry.kotlin

import platform.Foundation.NSRecursiveLock

public actual class ReentrantReadWriteLock {

    // visible to allow inlining
    public val impl: NSRecursiveLock = NSRecursiveLock()

    public actual inline fun <T> write(action: () -> T): T {
        impl.lock()
        try {
            return action()
        } finally {
            impl.unlock()
        }
    }

    // take perf hit of obtaining the same lock for now, for the sake of simplicity
    public actual inline fun <T> read(action: () -> T): T = write(action)
}
