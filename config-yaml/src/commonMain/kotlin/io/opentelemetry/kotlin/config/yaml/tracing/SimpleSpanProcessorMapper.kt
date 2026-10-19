package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.SimpleSpanProcessor

/**
 * Maps a `simple` span processor onto its behavior, including the exporter it feeds.
 */
@ExperimentalApi
fun SimpleSpanProcessor.toBehavior(): SimpleSpanProcessorBehavior? {
    val exporterBehavior = exporter.toBehavior()
    exporterBehavior ?: return null
    return SimpleSpanProcessorBehavior(exporter = exporterBehavior)
}
