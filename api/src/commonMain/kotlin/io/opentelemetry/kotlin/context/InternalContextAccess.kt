/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Temporary stand-in for [rootContext].
 */
@ExperimentalApi
@TempContextStandInApi
public fun internalRootContext(): Context = rootContext()

/**
 * Temporary stand-in for [currentContext].
 */
@ExperimentalApi
@TempContextStandInApi
public fun internalCurrentContext(): Context = currentContext()

/**
 * Temporary stand-in for [installContextStorage].
 */
@ExperimentalApi
@TempContextStandInApi
public fun internalInstallContextStorage(storage: CurrentContextStorage): Boolean =
    installContextStorage(storage)

/**
 * Temporary stand-in for [uninstallContextStorage].
 */
@ExperimentalApi
@TempContextStandInApi
public fun internalUninstallContextStorage(storage: CurrentContextStorage): Boolean =
    uninstallContextStorage(storage)
