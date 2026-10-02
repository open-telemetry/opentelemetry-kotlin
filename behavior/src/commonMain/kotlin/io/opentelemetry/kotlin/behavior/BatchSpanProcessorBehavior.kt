package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/** Batching settings for a span processor, in milliseconds where applicable. */
@ExperimentalApi
data class BatchSpanProcessorBehavior(
    val scheduleDelay: Long? = null,
    val exportTimeout: Long? = null,
    val maxQueueSize: Int? = null,
    val maxExportBatchSize: Int? = null,
) : Behavior<BatchSpanProcessorBehavior> {
    override fun mergeWith(higher: BatchSpanProcessorBehavior): BatchSpanProcessorBehavior = copy(
        scheduleDelay = higher.scheduleDelay ?: scheduleDelay,
        exportTimeout = higher.exportTimeout ?: exportTimeout,
        maxQueueSize = higher.maxQueueSize ?: maxQueueSize,
        maxExportBatchSize = higher.maxExportBatchSize ?: maxExportBatchSize,
    )
}
