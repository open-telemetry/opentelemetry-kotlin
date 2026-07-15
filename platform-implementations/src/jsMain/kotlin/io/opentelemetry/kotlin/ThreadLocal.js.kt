/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JavaScript implementation of [ThreadLocal]. The runtime is single-threaded, so this is a single
 * shared slot.
 */
public actual class ThreadLocal<T> actual constructor() {

    private var value: T? = null

    public actual fun get(): T? = value

    public actual fun set(value: T?) {
        this.value = value
    }
}
