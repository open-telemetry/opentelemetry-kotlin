/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * Kotlin/Native implementation of [AtomicReference] backed by [kotlin.concurrent.AtomicReference].
 */
public actual class AtomicReference<T> actual constructor(initialValue: T) {

    private val impl: kotlin.concurrent.AtomicReference<T> = kotlin.concurrent.AtomicReference(initialValue)

    public actual fun get(): T = impl.value

    public actual fun set(value: T) {
        impl.value = value
    }

    public actual fun compareAndSet(expect: T, update: T): Boolean = impl.compareAndSet(expect, update)
}
