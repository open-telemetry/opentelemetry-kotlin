/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JavaScript implementation of [AtomicReference]. JS is single-threaded, so a plain field suffices.
 */
public actual class AtomicReference<T> actual constructor(initialValue: T) {

    private var value: T = initialValue

    public actual fun get(): T = value

    public actual fun set(value: T) {
        this.value = value
    }

    public actual fun compareAndSet(expect: T, update: T): Boolean {
        if (value !== expect) {
            return false
        }
        value = update
        return true
    }
}
