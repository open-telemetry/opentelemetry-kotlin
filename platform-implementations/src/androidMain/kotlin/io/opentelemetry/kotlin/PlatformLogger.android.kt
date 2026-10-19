package io.opentelemetry.kotlin

import android.util.Log
import io.opentelemetry.kotlin.error.SdkErrorSeverity

public actual fun platformLog(message: String, severity: SdkErrorSeverity, throwable: Throwable?) {
    when (severity) {
        SdkErrorSeverity.INFO -> Log.i("OpenTelemetry", message, throwable)
        SdkErrorSeverity.WARNING -> Log.w("OpenTelemetry", message, throwable)
        SdkErrorSeverity.ERROR -> Log.e("OpenTelemetry", message, throwable)
    }
}
