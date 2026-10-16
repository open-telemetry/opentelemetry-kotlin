package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fputs
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
public actual fun platformLog(message: String, severity: SdkErrorSeverity, throwable: Throwable?) {
    val text = "[$severity] $message ${throwable?.stackTraceToString().orEmpty()}".trimEnd()
    fputs("$text\n", stderr)
}
