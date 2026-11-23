/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * An object reference that may be updated atomically.
 *
 * This is required as Kotlin's common AtomicReference class was only
 * added as experimental in 2.1 and we currently support back to 2.0:
 * https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.concurrent.atomics/-atomic-reference/
 */
public expect class AtomicReference<T>(initialValue: T) {

    /**
     * Gets the current value.
     */
    public fun get(): T

    /**
     * Sets the value unconditionally.
     */
    public fun set(value: T)

    /**
     * Atomically sets the value to [update] if the current value is the same instance as [expect].
     * Comparison is by identity (`===`), not `equals`.
     *
     * @return true if successful, false if the current value was not [expect]
     */
    public fun compareAndSet(expect: T, update: T): Boolean
}
