package io.opentelemetry.kotlin

import platform.Foundation.NSLog

public actual fun platformLog(message: String, level: PlatformLogLevel, throwable: Throwable?) {
    val text = "[$level] $message ${throwable?.stackTraceToString().orEmpty()}".trimEnd()
    // escape '%' so the text is not treated as a format specifier. Kotlin strings can't be passed
    // as a vararg to NSLog as they are not bridged to NSString, which crashes.
    NSLog(text.replace("%", "%%"))
}
