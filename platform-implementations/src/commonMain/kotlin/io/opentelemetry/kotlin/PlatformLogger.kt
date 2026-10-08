package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorSeverity

/**
 * Platform-specific logger that routes logs to the appropriate destination at a level matching
 * [severity] (e.g., Logcat on Android, NSLog on Apple platforms, console on JS, stdout on JVM).
 */
public expect fun platformLog(
    message: String,
    severity: SdkErrorSeverity = SdkErrorSeverity.INFO,
    throwable: Throwable? = null,
)
