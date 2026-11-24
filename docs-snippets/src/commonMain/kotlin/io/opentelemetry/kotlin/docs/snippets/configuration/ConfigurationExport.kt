package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.compositeLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.stdoutLogRecordExporter
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.stdoutSpanExporter

// region configuration-export
fun initWithSeveralProcessors(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        // Each export {} call adds a processor. They run in the order they're declared.
        export { batchSpanProcessor(otlpHttpSpanExporter()) }
        export { simpleSpanProcessor(stdoutSpanExporter()) }
    }
    loggerProvider {
        // export {} can only be called once here, so combine processors explicitly.
        export {
            compositeLogRecordProcessor(
                batchLogRecordProcessor(otlpHttpLogRecordExporter()),
                simpleLogRecordProcessor(stdoutLogRecordExporter()),
            )
        }
    }
}
// end-region
