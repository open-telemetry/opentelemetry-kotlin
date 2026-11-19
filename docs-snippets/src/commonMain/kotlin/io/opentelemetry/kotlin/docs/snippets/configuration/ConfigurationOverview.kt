package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.propagation.createW3CTraceContextPropagator
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter

// region configuration-overview
fun initConfiguredTelemetry(): OpenTelemetrySdk = createOpenTelemetry {
    // Applies to every signal
    serviceName = "checkout-service"
    resource { setStringAttribute("deployment.environment.name", "production") }
    attributeLimits { attributeCountLimit = 64 }
    propagator { createW3CTraceContextPropagator() }

    // Applies to one signal
    tracerProvider {
        sampler { parentBased(root = probability(0.25)) }
        spanLimits { eventCountLimit = 32 }
        export { batchSpanProcessor(otlpHttpSpanExporter { endpoint = "https://collector.example.com" }) }
    }
    loggerProvider {
        export { batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = "https://collector.example.com" }) }
    }
}
// end-region
