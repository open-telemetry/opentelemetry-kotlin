package io.opentelemetry.kotlin

import java.util.logging.Level
import java.util.logging.Logger

private val logger = Logger.getLogger("io.opentelemetry.kotlin")

public actual fun platformLog(message: String, level: PlatformLogLevel, throwable: Throwable?) {
    val javaLevel = when (level) {
        PlatformLogLevel.INFO -> Level.INFO
        PlatformLogLevel.WARNING -> Level.WARNING
        PlatformLogLevel.ERROR -> Level.SEVERE
    }
    try {
        logger.log(javaLevel, message, throwable)
    } catch (ignored: Throwable) {
        if (ignored is InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
