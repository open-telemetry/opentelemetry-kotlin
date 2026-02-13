package io.opentelemetry.kotlin

import android.util.Log

public actual fun platformLog(message: String) {
    Log.d("OpenTelemetry", message)
}
