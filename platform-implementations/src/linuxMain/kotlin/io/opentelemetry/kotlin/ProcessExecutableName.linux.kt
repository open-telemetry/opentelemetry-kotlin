/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import okio.FileSystem
import okio.Path.Companion.toPath

public actual fun getProcessExecutableName(): String? = runCatching {
    FileSystem.SYSTEM.metadataOrNull("/proc/self/exe".toPath())?.symlinkTarget?.name
}.getOrNull()
