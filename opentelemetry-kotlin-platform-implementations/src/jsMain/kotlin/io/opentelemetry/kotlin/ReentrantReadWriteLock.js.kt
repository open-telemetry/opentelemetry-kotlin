package io.opentelemetry.kotlin

public actual class ReentrantReadWriteLock {
    public actual inline fun <T> write(action: () -> T): T = action()
    public actual inline fun <T> read(action: () -> T): T = action()
}
