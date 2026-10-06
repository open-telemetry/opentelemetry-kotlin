package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorSeverity

public actual fun platformLog(message: String, severity: SdkErrorSeverity, throwable: Throwable?) {
    val text = "$message ${throwable?.stackTraceToString().orEmpty()}".trimEnd()
    when (severity) {
        SdkErrorSeverity.INFO -> console.info(text)
        SdkErrorSeverity.WARNING -> console.warn(text)
        SdkErrorSeverity.ERROR -> console.error(text)
    }
}
