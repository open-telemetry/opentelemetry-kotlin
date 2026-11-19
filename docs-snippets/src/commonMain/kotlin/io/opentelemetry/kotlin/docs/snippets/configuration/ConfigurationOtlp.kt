package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter

// region configuration-otlp
fun initWithTunedExport(fetchToken: suspend () -> String): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        export {
            batchSpanProcessor(
                exporter = otlpHttpSpanExporter {
                    endpoint = "https://collector.example.com" // /v1/traces is appended
                    timeoutMs = 5_000
                    headers = { mapOf("Authorization" to "Bearer ${fetchToken()}") } // called per export
                },
                maxQueueSize = 4096, // spans buffered before new ones are dropped
                scheduleDelayMs = 2_000, // how often a batch is exported
                maxExportBatchSize = 256,
                exportTimeoutMs = 10_000,
            )
        }
    }
}
// end-region
