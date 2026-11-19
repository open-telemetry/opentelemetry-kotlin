package io.opentelemetry.kotlin.docs.snippets.gettingstarted

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.stdoutLogRecordExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.stdoutSpanExporter

// region getting-started-init
fun initTelemetry(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service" // shown as service.name in your backend
    tracerProvider {
        export { simpleSpanProcessor(stdoutSpanExporter()) }
    }
    loggerProvider {
        export { simpleLogRecordProcessor(stdoutLogRecordExporter()) }
    }
}
// end-region
