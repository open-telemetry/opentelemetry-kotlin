package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry

// region tracing-limits
fun initTracingWithLimits(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        spanLimits {
            attributeCountLimit = 32 // extra attributes are dropped
            attributeValueLengthLimit = 1024 // longer string values are truncated
            eventCountLimit = 64 // extra events are dropped
            linkCountLimit = 16 // extra links are dropped
        }
    }
}
// end-region
