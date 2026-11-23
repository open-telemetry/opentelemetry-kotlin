/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.kotlin

import platform.Foundation.NSProcessInfo

public actual fun getProcessExecutableName(): String? = runCatching {
    NSProcessInfo.processInfo.processName
}.getOrNull()
