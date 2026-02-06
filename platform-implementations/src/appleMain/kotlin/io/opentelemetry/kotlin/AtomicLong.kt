/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import platform.Foundation.NSLock

public actual class AtomicLong actual constructor(initialValue: Long) {

    private val lock = NSLock()
    private var value: Long = initialValue

    public actual fun get(): Long = withLock { value }

    public actual fun set(newValue: Long) {
        withLock { value = newValue }
    }

    public actual fun incrementAndGet(): Long = withLock { ++value }

    public actual fun decrementAndGet(): Long = withLock { --value }

    public actual fun addAndGet(delta: Long): Long = withLock {
        value += delta
        value
    }

    public actual fun getAndAdd(delta: Long): Long = withLock {
        val oldValue = value
        value += delta
        oldValue
    }

    public actual fun compareAndSet(expect: Long, update: Long): Boolean = withLock {
        if (value == expect) {
            value = update
            true
        } else {
            false
        }
    }

    private inline fun <T> withLock(action: () -> T): T {
        lock.lock()
        try {
            return action()
        } finally {
            lock.unlock()
        }
    }
}
