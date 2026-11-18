package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry

// region configuration-limits
fun initWithLimits(): OpenTelemetrySdk = createOpenTelemetry {
    attributeLimits {
        attributeCountLimit = 64 // the default for every signal
        attributeValueLengthLimit = 1024
    }
    tracerProvider {
        spanLimits {
            attributeCountLimit = 128 // overrides the global limit for spans
            eventCountLimit = 32
            linkCountLimit = 32
            attributeCountPerEventLimit = 16
            attributeCountPerLinkLimit = 16
        }
    }
    loggerProvider {
        logLimits { attributeValueLengthLimit = 4096 }
    }
}
// end-region
