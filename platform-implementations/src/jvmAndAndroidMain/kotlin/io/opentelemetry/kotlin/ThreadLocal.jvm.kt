/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JVM/Android implementation of [ThreadLocal] backed by [java.lang.ThreadLocal].
 */
public actual class ThreadLocal<T> actual constructor() {

    private val impl = java.lang.ThreadLocal<T>()

    public actual fun get(): T? = impl.get()

    public actual fun set(value: T?) {
        impl.set(value)
    }
}
