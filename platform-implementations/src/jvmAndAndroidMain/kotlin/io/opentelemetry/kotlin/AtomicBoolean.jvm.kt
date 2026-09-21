/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JVM/Android implementation of [AtomicBoolean] backed by
 * [java.util.concurrent.atomic.AtomicBoolean].
 */
public actual class AtomicBoolean actual constructor(initialValue: Boolean) {

    private val impl: java.util.concurrent.atomic.AtomicBoolean =
        java.util.concurrent.atomic.AtomicBoolean(initialValue)

    public actual fun get(): Boolean = impl.get()
    public actual fun set(newValue: Boolean): Unit = impl.set(newValue)
    public actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean =
        impl.compareAndSet(expect, update)
}
