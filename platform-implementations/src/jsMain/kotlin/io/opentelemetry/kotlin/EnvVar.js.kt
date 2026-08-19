/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * Reads from `process.env`, which is only available on Node.js. The `typeof` guard means a browser,
 * where `process` is undefined, gets `null` rather than a `ReferenceError`.
 */
public actual fun getEnvVarValue(name: String): String? {
    val env = js("(typeof process !== 'undefined' && process) ? process.env : {}")
    return env[name] as? String
}
