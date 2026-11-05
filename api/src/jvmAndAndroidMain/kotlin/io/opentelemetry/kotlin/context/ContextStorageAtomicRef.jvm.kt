/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

import java.util.concurrent.atomic.AtomicReference

internal actual class ContextStorageAtomicRef<T> actual constructor(initialValue: T) {
    private val impl = AtomicReference(initialValue)

    actual fun get(): T = impl.get()

    actual fun set(value: T) {
        impl.set(value)
    }

    actual fun compareAndSet(expect: T, update: T): Boolean = impl.compareAndSet(expect, update)
}
