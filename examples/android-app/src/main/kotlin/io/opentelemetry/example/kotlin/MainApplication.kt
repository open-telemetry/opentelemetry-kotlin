package io.opentelemetry.example.kotlin

import android.app.Application
import io.opentelemetry.example.kotlin.instantiateOtelApi
import io.opentelemetry.example.kotlin.runLoggingExamples
import io.opentelemetry.example.kotlin.runTracingExamples
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry

@OptIn(ExperimentalApi::class)
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val endpoint = null // supply a URL string here to export telemetry
        val otelApi: OpenTelemetry = instantiateOtelApi(endpoint)
        runTracingExamples(otelApi)
        runLoggingExamples(otelApi)
    }
}
