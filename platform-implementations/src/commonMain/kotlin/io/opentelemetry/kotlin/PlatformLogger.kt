package io.opentelemetry.kotlin

/**
 * Platform-specific logger that routes logs to the appropriate destination
 * (e.g., Logcat on Android, NSLog on Apple platforms, console on JS, stdout on JVM).
 */
public expect fun platformLog(message: String)
