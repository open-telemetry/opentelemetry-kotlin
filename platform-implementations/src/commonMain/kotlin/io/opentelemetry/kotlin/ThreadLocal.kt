/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * Holds a value that is confined to the thread that set it. Each thread observes its own value
 * independently of others, and reads return `null` until that thread has set a value.
 *
 * This is the multiplatform analogue of `java.lang.ThreadLocal`.
 */
public expect class ThreadLocal<T>() {

    /**
     * Returns the value set by the current thread, if any
     */
    public fun get(): T?

    /**
     * Sets the value for the current thread
     */
    public fun set(value: T?)
}
