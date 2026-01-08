package io.opentelemetry.example.kotlin

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry

@OptIn(ExperimentalApi::class)
fun main() {
    val otelApi: OpenTelemetry = instantiateOtelApi(null)
    runTracingExamples(otelApi)
    runLoggingExamples(otelApi)
}
