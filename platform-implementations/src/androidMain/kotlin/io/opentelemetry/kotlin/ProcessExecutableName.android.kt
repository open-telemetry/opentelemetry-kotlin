/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import java.io.File

/**
 * Every Android app runs from the same `app_process` executable, so this returns the process name
 * from  `Application.getProcessName()` on API 28+, then `ActivityThread.currentProcessName()`.
 */
public actual fun getProcessExecutableName(): String? =
    processNameFromApplication() ?: processNameFromActivityThread() ?: processNameFromCmdline()

private fun processNameFromApplication(): String? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Application.getProcessName().takeIf { it.isNotBlank() }
    } else {
        null
    }
}.getOrNull()

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
private fun processNameFromActivityThread(): String? = runCatching {
    Class.forName("android.app.ActivityThread")
        .getDeclaredMethod("currentProcessName")
        .invoke(null) as? String
}.getOrNull()?.takeIf { it.isNotBlank() }

private fun processNameFromCmdline(): String? = runCatching {
    File("/proc/self/cmdline").readText().substringBefore('\u0000').takeIf { it.isNotBlank() }
}.getOrNull()
