package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor

/**
 * Maps `logger_provider.processors` onto exporter behavior. Console is the only exporter this
 * mapper understands: selecting it in any simple or batch processor is the whole configuration.
 * If multiple processors are configured, the first one is used.
 */
@ExperimentalApi
fun List<LogRecordProcessor>.toExporterBehavior(): LogExporterBehavior? {
    val firstProcessor = firstOrNull() ?: return null

    return when {
        firstProcessor.simple?.exporter?.console != null -> LogExporterBehavior.Console
        firstProcessor.batch?.exporter?.console != null -> LogExporterBehavior.Console
        else -> null
    }
}
