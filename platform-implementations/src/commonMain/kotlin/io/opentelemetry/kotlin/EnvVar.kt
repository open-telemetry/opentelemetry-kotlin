/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * Returns the value of the environment variable with the given [name], or `null` if the variable
 * is not set. Platforms that do not use envars (e.g. Android) will always return null.
 */
public expect fun getEnvVarValue(name: String): String?
