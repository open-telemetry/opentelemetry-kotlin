/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import platform.Foundation.NSNumber
import platform.Foundation.NSThread
import platform.Foundation.numberWithLong

private val instanceCounter = AtomicLong()

/**
 * Apple implementation of [ThreadLocal] backed by each thread's
 * [NSThread.threadDictionary]. Every instance uses a unique key so separate instances stay
 * independent.
 */
public actual class ThreadLocal<T> actual constructor() {

    private val key: NSNumber = NSNumber.numberWithLong(instanceCounter.incrementAndGet())

    @Suppress("UNCHECKED_CAST")
    public actual fun get(): T? =
        NSThread.currentThread.threadDictionary.objectForKey(key) as? T

    public actual fun set(value: T?) {
        val dictionary = NSThread.currentThread.threadDictionary
        if (value == null) {
            dictionary.removeObjectForKey(key)
        } else {
            dictionary.setObject(value, key)
        }
    }
}
