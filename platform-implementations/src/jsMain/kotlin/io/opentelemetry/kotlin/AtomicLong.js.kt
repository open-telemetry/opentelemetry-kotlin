/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JavaScript implementation of [AtomicLong].
 */
public actual class AtomicLong actual constructor(initialValue: Long) {

    private var value: Long = initialValue

    public actual fun get(): Long = value

    public actual fun set(newValue: Long) {
        value = newValue
    }

    public actual fun incrementAndGet(): Long = ++value

    public actual fun decrementAndGet(): Long = --value

    public actual fun addAndGet(delta: Long): Long {
        value += delta
        return value
    }

    public actual fun getAndAdd(delta: Long): Long {
        val oldValue = value
        value += delta
        return oldValue
    }

    public actual fun compareAndSet(expect: Long, update: Long): Boolean {
        return if (value == expect) {
            value = update
            true
        } else {
            false
        }
    }
}
