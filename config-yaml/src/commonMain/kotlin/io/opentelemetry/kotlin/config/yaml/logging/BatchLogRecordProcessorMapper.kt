package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchLogRecordProcessor

/**
 * Maps `logger_provider.processors.batch` onto batch log record processor behavior.
 */
@ExperimentalApi
fun BatchLogRecordProcessor.toBehavior(): BatchLogRecordProcessorBehavior {
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
    return BatchLogRecordProcessorBehavior(
        scheduleDelay = scheduleDelay,
        exportTimeout = exportTimeout,
        maxQueueSize = maxQueueSize,
        maxExportBatchSize = maxExportBatchSize,
        exporter = exporter.toBehavior(),
    )
}
