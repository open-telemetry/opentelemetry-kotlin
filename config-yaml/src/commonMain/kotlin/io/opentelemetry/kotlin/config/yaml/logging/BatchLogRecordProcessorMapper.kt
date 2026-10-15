package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchLogRecordProcessor

/**
 * Maps`tracer_provider.processors.items` onto batch processor behavior.
 */
@ExperimentalApi
fun BatchLogRecordProcessor.toBehavior(): BatchLogRecordProcessorBehavior {
    val scheduleDelay = scheduleDelay?.takeIf { value -> value >= 0 }
        ?: BatchLogRecordProcessorBehavior.DEFAULT_SCHEDULE_DELAY
    val exportTimeout = exportTimeout?.takeIf { value -> value >= 0 }
        ?: BatchLogRecordProcessorBehavior.DEFAULT_EXPORT_TIMEOUT
    val maxQueueSize = maxQueueSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt()
        ?: BatchLogRecordProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE
    val maxExportBatchSize = maxExportBatchSize?.takeIf { value -> value in 1..Int.MAX_VALUE.toLong() }?.toInt()
        ?: BatchLogRecordProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE
    return BatchLogRecordProcessorBehavior(
        scheduleDelay = scheduleDelay,
        exportTimeout = exportTimeout,
        maxQueueSize = maxQueueSize,
        maxExportBatchSize = maxExportBatchSize,
        exporter = exporter.toBehavior(),
    )
}
