package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor

/**
 * Maps `logger_provider.processors` onto processor behavior. Console is the only exporter this
 * mapper understands: selecting it in any simple or batch processor is the whole configuration.
 * Anything else is left unset.
 */
@ExperimentalApi
fun List<LogRecordProcessor>.toBehavior(): LogRecordProcessorBehavior? {
    val consoleSelected = any { processor ->
        processor.simple?.exporter?.console != null ||
            processor.batch?.exporter?.console != null
    }
    if (!consoleSelected) {
        return null
    }
    return LogRecordProcessorBehavior(console = ConsoleExporterBehavior())
}
