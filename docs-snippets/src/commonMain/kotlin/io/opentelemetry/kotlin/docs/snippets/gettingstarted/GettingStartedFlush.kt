package io.opentelemetry.kotlin.docs.snippets.gettingstarted

import io.opentelemetry.kotlin.OpenTelemetrySdk

// region getting-started-flush
// Exports everything buffered so far. The SDK keeps running.
suspend fun flushTelemetry(otel: OpenTelemetrySdk) {
    otel.forceFlush()
}

// Flushes, then stops the SDK. Telemetry recorded afterwards is dropped.
suspend fun stopTelemetry(otel: OpenTelemetrySdk) {
    otel.shutdown()
}
// end-region
