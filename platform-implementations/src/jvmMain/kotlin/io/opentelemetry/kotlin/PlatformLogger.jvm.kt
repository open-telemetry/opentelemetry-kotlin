package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorSeverity

public actual fun platformLog(message: String, severity: SdkErrorSeverity, throwable: Throwable?) {
    val prefix = when (severity) {
        SdkErrorSeverity.INFO -> ""
        SdkErrorSeverity.WARNING, SdkErrorSeverity.ERROR -> "[$severity] "
    }
    println("$prefix$message ${throwable?.stackTraceToString().orEmpty()}".trimEnd())
}
