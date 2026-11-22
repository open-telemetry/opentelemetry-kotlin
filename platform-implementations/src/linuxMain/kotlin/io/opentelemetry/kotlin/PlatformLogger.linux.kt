package io.opentelemetry.kotlin

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fputs
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
public actual fun platformLog(message: String, level: PlatformLogLevel, throwable: Throwable?) {
    val text = "[$level] $message ${throwable?.stackTraceToString().orEmpty()}".trimEnd()
    fputs("$text\n", stderr)
}
