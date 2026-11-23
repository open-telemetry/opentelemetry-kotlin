package io.opentelemetry.kotlin

/**
 * Platform-specific logger that routes logs to the appropriate destination at a level matching
 * [level] (e.g., Logcat on Android, NSLog on Apple platforms, console on JS, java.util.logging on JVM).
 */
public expect fun platformLog(
    message: String,
    level: PlatformLogLevel = PlatformLogLevel.INFO,
    throwable: Throwable? = null,
)
