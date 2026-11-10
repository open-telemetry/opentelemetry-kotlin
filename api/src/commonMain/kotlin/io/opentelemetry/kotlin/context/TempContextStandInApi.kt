/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin.context

/**
 * Marks temporary public stand-ins for internal context functions in `:api`, so that other modules
 * in this project can call them before those functions become public.
 *
 * The stand-ins will be deleted without notice. Applications and instrumentation must not call
 * them.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class TempContextStandInApi
