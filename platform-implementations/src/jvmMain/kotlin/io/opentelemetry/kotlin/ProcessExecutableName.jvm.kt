/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import java.io.File

public actual fun getProcessExecutableName(): String? = runCatching {
    ProcessHandle.current().info().command().map { File(it).name }.orElse(null)
}.getOrNull()
