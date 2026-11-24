package io.opentelemetry.kotlin.docs.snippets.gettingstarted

import android.app.Application
import io.opentelemetry.kotlin.OpenTelemetrySdk

// region getting-started-android
class MyApplication : Application() {
    lateinit var otel: OpenTelemetrySdk

    override fun onCreate() {
        super.onCreate()
        otel = initTelemetryWithOtlp()
    }
}
// end-region
