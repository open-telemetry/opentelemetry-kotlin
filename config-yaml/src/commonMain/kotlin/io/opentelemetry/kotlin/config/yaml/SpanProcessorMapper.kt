package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor

/**
 * Maps `tracer_provider.processors` onto exporter behavior. Console is the only exporter this
 * mapper understands: selecting it in any simple or batch processor is the whole configuration.
 * If multiple processors are configured, the first one is used.
 */
@ExperimentalApi
fun List<SpanProcessor>.toExporterBehavior(): SpanExporterBehavior? {
    val firstProcessor = firstOrNull() ?: return null

    return when {
        firstProcessor.simple?.exporter?.console != null -> SpanExporterBehavior.Console
        firstProcessor.batch?.exporter?.console != null -> SpanExporterBehavior.Console
        else -> null
    }
}
