package io.opentelemetry.kotlin.docs.snippets.compat

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter

// region compat-create
fun initCompatTelemetry(): OpenTelemetrySdk = createCompatOpenTelemetry {
    serviceName = "checkout-service"
    tracerProvider {
        export { batchSpanProcessor(otlpHttpSpanExporter { endpoint = "http://localhost:4318" }) }
    }
}
// end-region
