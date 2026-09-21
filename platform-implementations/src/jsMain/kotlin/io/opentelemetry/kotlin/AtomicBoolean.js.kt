/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JavaScript implementation of [AtomicBoolean].
 */
public actual class AtomicBoolean actual constructor(initialValue: Boolean) {

    private var value: Boolean = initialValue

    public actual fun get(): Boolean = value

    public actual fun set(newValue: Boolean) {
        value = newValue
    }

    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
        return if (value == expect) {
            value = update
            true
        } else {
            false
        }
    }
}
