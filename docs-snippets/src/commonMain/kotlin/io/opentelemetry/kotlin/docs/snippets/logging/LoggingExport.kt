package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.compositeLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.stdoutLogRecordExporter

// region logging-export
fun initLogging(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service"
    loggerProvider {
        export {
            compositeLogRecordProcessor(
                batchLogRecordProcessor(otlpHttpLogRecordExporter { endpoint = "http://localhost:4318" }),
                simpleLogRecordProcessor(stdoutLogRecordExporter()),
            )
        }
    }
}
// end-region
