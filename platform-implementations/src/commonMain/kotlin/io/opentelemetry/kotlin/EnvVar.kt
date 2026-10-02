/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * Returns the value of the environment variable with the given [name], or `null` if the variable
 * is not set.
 *
 * Only the JVM and Node.js read envars. Android, Apple, and browser platforms always return `null`
 * because an app's process environment is controlled by the host or tooling rather than the app.
 */
public expect fun getEnvVarValue(name: String): String?
