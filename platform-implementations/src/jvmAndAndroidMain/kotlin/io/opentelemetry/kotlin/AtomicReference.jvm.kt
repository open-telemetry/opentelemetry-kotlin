/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JVM/Android implementation of [AtomicReference] backed by [java.util.concurrent.atomic.AtomicReference].
 */
public actual class AtomicReference<T> actual constructor(initialValue: T) {

    private val impl: java.util.concurrent.atomic.AtomicReference<T> =
        java.util.concurrent.atomic.AtomicReference(initialValue)

    public actual fun get(): T = impl.get()

    public actual fun set(value: T): Unit = impl.set(value)

    public actual fun compareAndSet(expect: T, update: T): Boolean = impl.compareAndSet(expect, update)
}
