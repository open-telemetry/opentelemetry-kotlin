/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Returns the current [Context] from the installed [CurrentContextStorage], or [rootContext]
 * if none is installed.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/context/#get-current-context
 */
@ExperimentalApi
internal fun currentContext(): Context = ProcessWideContextStorageSlot.installed()?.current() ?: rootContext()

/**
 * Makes this [Context] current while [block] runs, then restores the previous context, even if
 * [block] throws.
 *
 * Like [Context.attach], this does not change the current context when no [CurrentContextStorage]
 * is installed, but [block] still runs.
 */
@ExperimentalApi
internal inline fun <T> Context.runWithContext(block: () -> T): T {
    val scope = attach()
    try {
        return block()
    } finally {
        scope.detach()
    }
}
