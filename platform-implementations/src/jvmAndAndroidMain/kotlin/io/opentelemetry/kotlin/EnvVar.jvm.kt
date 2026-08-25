/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

public actual fun getEnvVarValue(name: String): String? = System.getenv(name)
