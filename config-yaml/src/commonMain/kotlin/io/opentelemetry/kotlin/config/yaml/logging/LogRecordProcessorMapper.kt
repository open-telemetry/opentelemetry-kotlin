package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor
import io.opentelemetry.kotlin.config.yaml.toBehavior

/**
 * Maps`logger_provider.processors` onto processor behavior.
 */
@ExperimentalApi
fun List<LogRecordProcessor>.toBehavior(): LogRecordProcessorBehavior? {
    // TODO: Add support to multiple exporters being in use simultaneously once we have the LogRecordProcessor
    //  fully implemented.
    for (processor in this) {
        val simple = processor.simple?.toBehavior()
        val batch = processor.batch?.toBehavior()

        val consoleExporter = processor.simple?.exporter?.console ?: processor.batch?.exporter?.console
        if (consoleExporter != null) {
            return LogRecordProcessorBehavior(console = ConsoleExporterBehavior(), simple = simple, batch = batch)
        }

        val httpExporter = processor.simple?.exporter?.otlpHttp ?: processor.batch?.exporter?.otlpHttp
        if (httpExporter != null) {
            return LogRecordProcessorBehavior(http = httpExporter.toBehavior(), simple = simple, batch = batch)
        }
    }

    return null
}
