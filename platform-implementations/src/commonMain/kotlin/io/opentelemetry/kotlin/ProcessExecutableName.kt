/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

/**
 * Returns the name of the current process's executable, or `null` if it is not available.
 *
 * The JVM and Linux return the executable's file name, Apple returns the process name, Android
 * returns the process name (usually the app's package name), and Node.js returns `process.argv0`.
 * Browsers always return `null`. Never throws.
 */
public expect fun getProcessExecutableName(): String?
