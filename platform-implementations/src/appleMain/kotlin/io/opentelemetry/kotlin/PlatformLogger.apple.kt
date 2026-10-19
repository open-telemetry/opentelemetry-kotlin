package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorSeverity
import platform.Foundation.NSLog

public actual fun platformLog(message: String, severity: SdkErrorSeverity, throwable: Throwable?) {
    val text = "[$severity] $message ${throwable?.stackTraceToString().orEmpty()}".trimEnd()
    // escape '%' so the text is not treated as a format specifier. Kotlin strings can't be passed
    // as a vararg to NSLog as they are not bridged to NSString, which crashes.
    NSLog(text.replace("%", "%%"))
}
