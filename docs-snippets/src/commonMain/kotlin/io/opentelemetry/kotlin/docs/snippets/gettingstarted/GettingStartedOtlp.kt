package io.opentelemetry.kotlin.docs.snippets.gettingstarted

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter

// region getting-started-otlp
const val COLLECTOR_URL = "http://localhost:4318" // use http://10.0.2.2:4318 on the Android emulator

fun initTelemetryWithOtlp(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service"
    tracerProvider {
        export { batchSpanProcessor(otlpHttpSpanExporter { endpoint = COLLECTOR_URL }) }
    }
    loggerProvider {
        export { batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = COLLECTOR_URL }) }
    }
}
// end-region
