package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchSpanProcessor

/**
 * Maps `tracer_provider.processors.batch` onto batch span processor behavior.
 */
@ExperimentalApi
fun BatchSpanProcessor.toBehavior(): BatchSpanProcessorBehavior {
    val scheduleDelay = scheduleDelay?.takeIf { value -> value >= 0 }
        ?: BatchSpanProcessorBehavior.DEFAULT_SCHEDULE_DELAY
    val exportTimeout = exportTimeout?.takeIf { value -> value >= 0 }
        ?: BatchSpanProcessorBehavior.DEFAULT_EXPORT_TIMEOUT
    val maxQueueSize = maxQueueSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt()
        ?: BatchSpanProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE
    val requestedBatchSize = maxExportBatchSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt()
        ?: BatchSpanProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE

    val maxExportBatchSize = minOf(requestedBatchSize, maxQueueSize)
    return BatchSpanProcessorBehavior(
        scheduleDelay = scheduleDelay,
        exportTimeout = exportTimeout,
        maxQueueSize = maxQueueSize,
        maxExportBatchSize = maxExportBatchSize,
        exporter = exporter.toBehavior(),
    )
}
