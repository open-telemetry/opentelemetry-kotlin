package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.SimpleLogRecordProcessor

/**
 * Maps a `simple` log record processor onto its behavior, including the exporter it feeds.
 */
@ExperimentalApi
fun SimpleLogRecordProcessor?.toBehavior(): SimpleLogRecordProcessorBehavior? {
    if (this == null) {
        return null
    }

    val exporterBehavior = exporter?.console?.let {
        LogRecordExporterBehavior(console = ConsoleExporterBehavior())
    }
    return SimpleLogRecordProcessorBehavior(exporter = exporterBehavior)
}
