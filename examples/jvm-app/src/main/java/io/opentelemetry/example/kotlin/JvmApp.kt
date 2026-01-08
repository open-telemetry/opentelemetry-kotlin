package io.opentelemetry.example.kotlin

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry

@OptIn(ExperimentalApi::class)
fun main() {
    val endpoint = null // supply a URL string here to export telemetry
    val otelApi: OpenTelemetry = instantiateOtelApi(endpoint)
    runTracingExamples(otelApi)
    runLoggingExamples(otelApi)
    Thread.sleep(1000)
}
