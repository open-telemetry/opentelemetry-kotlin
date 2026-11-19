package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.tracing.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.SimpleSpanProcessor

/**
 * Maps a `simple` span processor onto its behavior, including the exporter it feeds.
 */
@OptIn(ExperimentalApi::class)
fun SimpleSpanProcessor.toBehavior(): SimpleSpanProcessorBehavior =
    SimpleSpanProcessorBehavior(
        exporter = exporter.toBehavior()
    )
