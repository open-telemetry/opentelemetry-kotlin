package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter
import kotlin.time.Duration.Companion.seconds

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
                scheduleDelay = 2.seconds, // how often a batch is exported
                maxExportBatchSize = 256,
                exportTimeout = 10.seconds,
            )
        }
    }
}
// end-region
