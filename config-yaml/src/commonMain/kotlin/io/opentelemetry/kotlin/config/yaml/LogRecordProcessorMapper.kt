package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.OtlpExporter
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor

/**
 * Maps`logger_provider.processors` onto processor behavior.
 */
@ExperimentalApi
fun List<LogRecordProcessor>.toBehavior(): LogRecordProcessorBehavior? {
    // TODO: Add support to multiple exporters being in use simultaneously once we have the LogRecordProcessor
    //  fully implemented.
    for (processor in this) {
        val simple = processor.simple?.let { SimpleLogRecordProcessorBehavior() }
        val consoleExporter = processor.simple?.exporter?.console ?: processor.batch?.exporter?.console
        if (consoleExporter != null) {
            return LogRecordProcessorBehavior(console = ConsoleExporterBehavior(), simple = simple)
        }

        val httpExporter = processor.simple?.exporter?.otlpHttp ?: processor.batch?.exporter?.otlpHttp
        if (httpExporter != null) {
            // if there are duplicate keys, the last one wins.
            // The spec says that in the case of duplicate keys, [headers] have a higher precedence.
            val headers =
                OtlpExporter.buildHeaderMap(httpExporter.headersList).orEmpty() +
                    OtlpExporter.buildHeaderMap(
                        httpExporter.headers?.joinToString(separator = ",") { pair ->
                            "${pair.name}=${pair.value}"
                        }
                    ).orEmpty()
            val httpExporterBehavior = OtlpHttpLogsExporterBehavior(
                endpoint = httpExporter.endpoint,
                timeout = httpExporter.timeout,
                headers = headers.ifEmpty { null }
            )
            return LogRecordProcessorBehavior(http = httpExporterBehavior, simple = simple)
        }
    }

    return null
}
