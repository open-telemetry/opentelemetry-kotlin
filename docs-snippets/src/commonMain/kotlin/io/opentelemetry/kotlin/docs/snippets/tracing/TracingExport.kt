package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.compositeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.stdoutSpanExporter

// region tracing-export
fun initTracing(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service"
    tracerProvider {
        export {
            compositeSpanProcessor(
                batchSpanProcessor(otlpHttpSpanExporter { endpoint = "http://localhost:4318" }),
                simpleSpanProcessor(stdoutSpanExporter()),
            )
        }
    }
}
// end-region
