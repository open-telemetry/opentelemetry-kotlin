/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import platform.posix.readlink

private const val MAX_PATH_BYTES = 4096

/**
 * Reads the target of the `/proc/self/exe` symlink and returns its file name.
 */
@OptIn(ExperimentalForeignApi::class)
public actual fun getProcessExecutableName(): String? = runCatching {
    memScoped {
        val buffer = allocArray<ByteVar>(MAX_PATH_BYTES)
        val length = readlink("/proc/self/exe", buffer, MAX_PATH_BYTES.convert()).toInt()
        if (length <= 0) {
            null
        } else {
            buffer.readBytes(length).decodeToString().substringAfterLast('/').takeIf { it.isNotEmpty() }
        }
    }
}.getOrNull()
