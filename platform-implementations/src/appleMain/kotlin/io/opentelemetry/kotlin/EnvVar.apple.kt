/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * Apple apps do not control their process environment, so envars are never read.
 */
public actual fun getEnvVarValue(name: String): String? = null
