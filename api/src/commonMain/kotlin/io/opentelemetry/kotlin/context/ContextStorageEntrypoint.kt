/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Installs [storage] as the process-wide [CurrentContextStorage] if none is installed. Context
 * operations then go through [storage], wrapped so that misuse is reported to the process-wide
 * SdkErrorHandler and nothing [storage] throws escapes.
 *
 * Pass the same [storage] instance to [uninstallContextStorage] to remove it.
 *
 * @return true if installed, false if a storage was already installed
 */
@ExperimentalApi
public fun installContextStorage(storage: CurrentContextStorage): Boolean =
    ProcessWideContextStorageSlot.install(
        original = storage,
        effective = MisuseReportingCurrentContextStorage(storage, ::rootContext),
    )

/**
 * Uninstalls the process-wide [CurrentContextStorage] if [storage] is the instance that was passed
 * to [installContextStorage] (compared by identity).
 *
 * @return true if uninstalled, false if nothing is installed or another storage is installed
 */
@ExperimentalApi
public fun uninstallContextStorage(storage: CurrentContextStorage): Boolean =
    ProcessWideContextStorageSlot.uninstall(storage)
