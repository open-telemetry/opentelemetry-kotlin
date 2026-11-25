package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.tracing.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchSpanProcessor

/**
 * Maps `tracer_provider.processors.batch` onto batch span processor behavior.
 */
@OptIn(ExperimentalApi::class)
fun BatchSpanProcessor.toBehavior(): BatchSpanProcessorBehavior {
    val scheduleDelay = scheduleDelay?.takeIf { value -> value >= 0 }
    val exportTimeout = exportTimeout?.takeIf { value -> value >= 0 }
    val maxQueueSize = maxQueueSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt()
    val requestedBatchSize = maxExportBatchSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt()

    return BatchSpanProcessorBehavior(
        scheduleDelay = scheduleDelay,
        exportTimeout = exportTimeout,
        maxQueueSize = maxQueueSize,
        requestedQueueSize = requestedBatchSize,
        exporter = exporter.toBehavior(),
    )
}
