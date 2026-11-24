package io.opentelemetry.kotlin.docs.snippets.gettingstarted

import io.opentelemetry.kotlin.OpenTelemetrySdk

// region getting-started-ios-kotlin
// In the shared KMP module that your iOS framework is built from.
object Telemetry {
    private var otel: OpenTelemetrySdk? = null

    fun start() {
        if (otel == null) {
            otel = initTelemetryWithOtlp()
        }
    }
}
// end-region
