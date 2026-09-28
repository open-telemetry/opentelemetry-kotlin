package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Batching settings for the logger provider's processor. The exporter is configured on
 * [LogRecordProcessorBehavior]. Each null field is unset and defers to a lower configuration
 * layer or the SDK default.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor
 */
@ExperimentalApi
data class BatchLogRecordProcessorBehavior(
    /** Delay between consecutive exports, in milliseconds. Must be non-negative. */
    val scheduleDelay: Long? = null,
    /** Maximum export duration, in milliseconds. Zero means no limit. Must be non-negative. */
    val exportTimeout: Long? = null,
    /** Maximum number of queued log records. Must be positive. */
    val maxQueueSize: Int? = null,
    /** Maximum records per export. Must be positive and no greater than [maxQueueSize]. */
    val maxExportBatchSize: Int? = null,
) : Behavior<BatchLogRecordProcessorBehavior> {

    override fun mergeWith(higher: BatchLogRecordProcessorBehavior): BatchLogRecordProcessorBehavior = copy(
        scheduleDelay = higher.scheduleDelay ?: scheduleDelay,
        exportTimeout = higher.exportTimeout ?: exportTimeout,
        maxQueueSize = higher.maxQueueSize ?: maxQueueSize,
        maxExportBatchSize = higher.maxExportBatchSize ?: maxExportBatchSize,
    )
}
