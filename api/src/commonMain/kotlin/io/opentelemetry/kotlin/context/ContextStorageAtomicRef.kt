/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

/**
 * Minimal atomic reference implementation for the API's process-wide context state.
 */
internal expect class ContextStorageAtomicRef<T>(initialValue: T) {

    /**
     * Gets the current value.
     */
    fun get(): T

    /**
     * Sets the value unconditionally.
     */
    fun set(value: T)

    /**
     * Atomically sets the value to [update] if the current value is the same instance as [expect].
     * Comparison is by identity.
     *
     * @return true if successful
     */
    fun compareAndSet(expect: T, update: T): Boolean
}
