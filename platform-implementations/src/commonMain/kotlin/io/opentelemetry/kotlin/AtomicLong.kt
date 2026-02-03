/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * A Long value that may be updated atomically.
 *
 * This is required as Kotlin's AtomicLong class was only
 * added as experimental in 2.1 and we currently support back to 2.0:
 * https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.concurrent.atomics/-atomic-long/
 */
public expect class AtomicLong(initialValue: Long = 0L) {

    /**
     * Gets the current value.
     */
    public fun get(): Long

    /**
     * Sets to the given value.
     */
    public fun set(newValue: Long)

    /**
     * Atomically increments the current value by one and returns it.
     */
    public fun incrementAndGet(): Long

    /**
     * Atomically decrements by one the current value and returns it.
     */
    public fun decrementAndGet(): Long

    /**
     * Atomically adds the given value to the current value and returns the updated value.
     */
    public fun addAndGet(delta: Long): Long

    /**
     * Atomically adds the given value to the current value and returns the previous value.
     */
    public fun getAndAdd(delta: Long): Long

    /**
     * Atomically sets the value to the given updated value if the current value equals the
     * expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful, false if the actual value was not equal to the expected value
     */
    public fun compareAndSet(expect: Long, update: Long): Boolean
}
