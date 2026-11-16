package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
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

    val maxExportBatchSize = let {
        if (maxQueueSize != null && requestedBatchSize != null) {
            return@let minOf(requestedBatchSize, maxQueueSize)
        }
        requestedBatchSize ?: maxQueueSize
    }
    return BatchSpanProcessorBehavior(
        scheduleDelay = scheduleDelay,
        exportTimeout = exportTimeout,
        maxQueueSize = maxQueueSize,
        maxExportBatchSize = maxExportBatchSize,
        exporter = exporter.toBehavior(),
    )
}
