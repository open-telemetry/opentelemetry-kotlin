package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor

/**
 * Maps`tracer_provider.processors` onto processor behavior.
 */
@ExperimentalApi
fun List<SpanProcessor>.toBehavior(): SpanProcessorBehavior? {
    // TODO: Add support to multiple exporters being in use simultaneously once we have the SpanProcessor
    //  fully implemented.
    for (processor in this) {
        val consoleExporter = processor.simple?.exporter?.console ?: processor.batch?.exporter?.console
        if (consoleExporter != null) {
            return SpanProcessorBehavior(console = ConsoleExporterBehavior())
        }

        val httpExporter = processor.simple?.exporter?.otlpHttp ?: processor.batch?.exporter?.otlpHttp
        if (httpExporter != null) {
            val httpExporterBehavior = OtlpHttpExporterBehavior(
                endpoint = httpExporter.endpoint,
                timeout = httpExporter.timeout
            )
            return SpanProcessorBehavior(http = httpExporterBehavior)
        }
    }

    return null
}
