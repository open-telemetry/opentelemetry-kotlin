package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor

/**
 * Maps`logger_provider.processors` onto processor behavior.
 */
@ExperimentalApi
fun List<LogRecordProcessor>.toBehavior(): LogRecordProcessorBehavior? {
    // TODO: Add support to multiple exporters being in use simultaneously once we have the LogRecordProcessor
    //  fully implemented.
    for (processor in this) {
        val consoleExporter = processor.simple?.exporter?.console ?: processor.batch?.exporter?.console
        if (consoleExporter != null) {
            return LogRecordProcessorBehavior(console = ConsoleExporterBehavior())
        }

        val httpExporter = processor.simple?.exporter?.otlpHttp ?: processor.batch?.exporter?.otlpHttp
        if (httpExporter != null) {
            val httpExporterBehavior = OtlpHttpExporterBehavior(
                endpoint = httpExporter.endpoint,
                timeout = httpExporter.timeout
            )
            return LogRecordProcessorBehavior(http = httpExporterBehavior)
        }
    }

    return null
}
