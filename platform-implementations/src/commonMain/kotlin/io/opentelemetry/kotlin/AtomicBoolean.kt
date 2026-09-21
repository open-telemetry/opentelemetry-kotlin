/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * A Boolean value that may be updated atomically.
 *
 * This is required as Kotlin's AtomicBoolean class was only
 * added as experimental in 2.1 and we currently support back to 2.0:
 * https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.concurrent.atomics/-atomic-boolean/
 */
public expect class AtomicBoolean(initialValue: Boolean = false) {

    /**
     * Gets the current value.
     */
    public fun get(): Boolean

    /**
     * Sets to the given value.
     */
    public fun set(newValue: Boolean)

    /**
     * Atomically sets the value to the given updated value if the current value equals the
     * expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful, false if the actual value was not equal to the expected value
     */
    public fun compareAndSet(expect: Boolean, update: Boolean): Boolean
}
