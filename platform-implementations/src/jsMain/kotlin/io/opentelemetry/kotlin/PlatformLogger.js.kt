package io.opentelemetry.kotlin

public actual fun platformLog(message: String, level: PlatformLogLevel, throwable: Throwable?) {
    val text = "$message ${throwable?.stackTraceToString().orEmpty()}".trimEnd()
    when (level) {
        PlatformLogLevel.INFO -> console.info(text)
        PlatformLogLevel.WARNING -> console.warn(text)
        PlatformLogLevel.ERROR -> console.error(text)
    }
}
