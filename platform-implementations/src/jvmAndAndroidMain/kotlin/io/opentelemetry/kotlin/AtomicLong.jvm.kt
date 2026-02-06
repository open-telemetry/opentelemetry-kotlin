/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * JVM/Android implementation of [AtomicLong] backed by [java.util.concurrent.atomic.AtomicLong].
 *
 * Provides lock-free thread-safe operations using CPU compare-and-swap instructions.
 */
public actual class AtomicLong actual constructor(initialValue: Long) {

    /**
     * The underlying atomic implementation. Exposed for potential inlining optimizations.
     */
    private val impl: java.util.concurrent.atomic.AtomicLong =
        java.util.concurrent.atomic.AtomicLong(initialValue)

    public actual fun get(): Long = impl.get()
    public actual fun set(newValue: Long): Unit = impl.set(newValue)
    public actual fun incrementAndGet(): Long = impl.incrementAndGet()
    public actual fun decrementAndGet(): Long = impl.decrementAndGet()
    public actual fun addAndGet(delta: Long): Long = impl.addAndGet(delta)
    public actual fun getAndAdd(delta: Long): Long = impl.getAndAdd(delta)
    public actual fun compareAndSet(expect: Long, update: Long): Boolean =
        impl.compareAndSet(expect, update)
}
