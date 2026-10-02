package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
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
        val batch = processor.batch?.let {
            BatchSpanProcessorBehavior(
                scheduleDelay = it.scheduleDelay?.takeIf { value -> value >= 0 },
                exportTimeout = it.exportTimeout?.takeIf { value -> value >= 0 },
                maxQueueSize = it.maxQueueSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt(),
                maxExportBatchSize = it.maxExportBatchSize
                    ?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt(),
            )
        }
        val consoleExporter = processor.simple?.exporter?.console ?: processor.batch?.exporter?.console
        if (consoleExporter != null) {
            return SpanProcessorBehavior(console = ConsoleExporterBehavior(), batch = batch)
        }

        val httpExporter = processor.simple?.exporter?.otlpHttp ?: processor.batch?.exporter?.otlpHttp
        if (httpExporter != null) {
            // if there are duplicate keys, the last one wins.
            // The spec says that in the case of duplicate keys, [headers] have a higher precedence.
            val headers =
                OtlpHttpExporterBehavior.buildHeaderMap(httpExporter.headersList).orEmpty() +
                    OtlpHttpExporterBehavior.buildHeaderMap(
                        httpExporter.headers?.joinToString(separator = ",") {
                                pair ->
                            "${pair.name}=${pair.value}"
                        }
                    ).orEmpty()
            val httpExporterBehavior = OtlpHttpExporterBehavior(
                endpoint = httpExporter.endpoint,
                timeout = httpExporter.timeout,
                headers = headers.ifEmpty { null }
            )
            return SpanProcessorBehavior(http = httpExporterBehavior, batch = batch)
        }
    }

    return null
}
