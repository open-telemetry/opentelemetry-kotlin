package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry

// region logging-limits
fun initLoggingWithLimits(): OpenTelemetrySdk = createOpenTelemetry {
    loggerProvider {
        logLimits {
            attributeCountLimit = 32 // extra attributes are dropped
            attributeValueLengthLimit = 1024 // longer string values are truncated
        }
    }
}
// end-region
