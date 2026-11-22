package io.opentelemetry.kotlin

public actual fun platformLog(message: String, level: PlatformLogLevel, throwable: Throwable?) {
    val prefix = when (level) {
        PlatformLogLevel.INFO -> ""
        PlatformLogLevel.WARNING, PlatformLogLevel.ERROR -> "[$level] "
    }
    println("$prefix$message ${throwable?.stackTraceToString().orEmpty()}".trimEnd())
}
