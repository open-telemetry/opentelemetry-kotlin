/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import platform.Foundation.NSLock

public actual class AtomicBoolean actual constructor(initialValue: Boolean) {

    private val lock = NSLock()
    private var value: Boolean = initialValue

    public actual fun get(): Boolean = withLock { value }

    public actual fun set(newValue: Boolean) {
        withLock { value = newValue }
    }

    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean = withLock {
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
