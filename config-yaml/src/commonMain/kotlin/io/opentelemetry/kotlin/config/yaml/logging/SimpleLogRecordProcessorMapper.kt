package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.SimpleLogRecordProcessor

/**
 * Maps a `simple` log record processor onto its behavior, including the exporter it feeds.
 */
@ExperimentalApi
fun SimpleLogRecordProcessor.toBehavior(): SimpleLogRecordProcessorBehavior? {
    val exporterBehavior = exporter.toBehavior()
    exporterBehavior ?: return null
    return SimpleLogRecordProcessorBehavior(exporter = exporterBehavior)
}
