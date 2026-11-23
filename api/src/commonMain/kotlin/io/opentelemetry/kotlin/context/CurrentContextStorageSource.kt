/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Supplies the [CurrentContextStorage] that [Context.attach] dispatches to.
 */
@OptIn(ExperimentalApi::class)
internal fun interface CurrentContextStorageSource {

    /**
     * Returns the installed storage, or null if none is installed.
     */
    fun installed(): CurrentContextStorage?
}
