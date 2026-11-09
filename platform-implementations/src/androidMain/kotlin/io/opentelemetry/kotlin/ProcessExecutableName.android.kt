/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import java.io.File

/**
 * Every Android app runs from the same `app_process` executable, so this reads the process name
 * from `/proc/self/cmdline` instead. `Application.getProcessName()` needs API 28.
 */
public actual fun getProcessExecutableName(): String? = runCatching {
    File("/proc/self/cmdline").readText().substringBefore('\u0000').takeIf { it.isNotBlank() }
}.getOrNull()
