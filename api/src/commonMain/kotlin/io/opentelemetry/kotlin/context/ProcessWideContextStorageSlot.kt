/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.AtomicReference
import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Holds the process-wide [CurrentContextStorage]. At most one storage can be installed at a time,
 * and only the owner that installed it can uninstall it.
 *
 * Each installation records two storages: [Installed.original] is the storage the owner passed in
 * and uses to uninstall, while [Installed.effective] is the storage that context operations
 * dispatch to.
 */
@OptIn(ExperimentalApi::class)
internal object ProcessWideContextStorageSlot {

    internal class Installed(
        val original: CurrentContextStorage,
        val effective: CurrentContextStorage,
    )

    private val ref = AtomicReference<Installed?>(null)

    /**
     * Installs [effective] if the slot is empty.
     *
     * @return true if installed, false if a storage was already installed
     */
    fun install(original: CurrentContextStorage, effective: CurrentContextStorage): Boolean =
        ref.compareAndSet(null, Installed(original, effective))

    /**
     * Empties the slot if [original] is the same instance that was passed to [install].
     *
     * @return true if uninstalled, false if the slot is empty or owned by another storage
     */
    fun uninstall(original: CurrentContextStorage): Boolean {
        while (true) {
            val current = ref.get()
            if (current == null || current.original !== original) {
                return false
            }
            if (ref.compareAndSet(current, null)) {
                return true
            }
        }
    }

    /**
     * Returns the effective storage that is installed, or null if the slot is empty.
     */
    fun installed(): CurrentContextStorage? = ref.get()?.effective

    /**
     * Empties the slot regardless of owner.
     */
    fun reset() {
        ref.set(null)
    }
}
