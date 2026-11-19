package io.opentelemetry.kotlin.docs.snippets.gettingstarted

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry

// region getting-started-opt-in-call-site
@OptIn(ExperimentalApi::class)
fun recordStartup(otel: OpenTelemetry) {
    otel.tracerProvider.getTracer("com.example.startup").startSpan("app-start").end()
}
// end-region
