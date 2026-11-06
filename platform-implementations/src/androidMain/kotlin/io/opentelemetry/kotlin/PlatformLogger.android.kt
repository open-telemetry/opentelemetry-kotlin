package io.opentelemetry.kotlin

import android.util.Log

public actual fun platformLog(message: String, level: PlatformLogLevel, throwable: Throwable?) {
    when (level) {
        PlatformLogLevel.INFO -> Log.i("OpenTelemetry", message, throwable)
        PlatformLogLevel.WARNING -> Log.w("OpenTelemetry", message, throwable)
        PlatformLogLevel.ERROR -> Log.e("OpenTelemetry", message, throwable)
    }
}
