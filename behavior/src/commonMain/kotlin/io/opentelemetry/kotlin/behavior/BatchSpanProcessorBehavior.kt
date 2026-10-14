package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/** Batching settings for a span processor, in milliseconds where applicable. */
@ExperimentalApi
data class BatchSpanProcessorBehavior(
    /** Delay between consecutive exports, in milliseconds. Must be non-negative. */
    val scheduleDelay: Long? = null,
    /** Maximum export duration, in milliseconds. Zero means no limit. Must be non-negative. */
    val exportTimeout: Long? = null,
    /** Maximum number of queued log records. Must be positive. */
    val maxQueueSize: Int? = null,
    /** Maximum records per export. Must be positive and no greater than [maxQueueSize]. */
    val maxExportBatchSize: Int? = null,
    /** Span exporter. */
    val exporter: SpanExporterBehavior? = null
) : Behavior<BatchSpanProcessorBehavior> {
    override fun mergeWith(higher: BatchSpanProcessorBehavior): BatchSpanProcessorBehavior = copy(
        scheduleDelay = higher.scheduleDelay ?: scheduleDelay,
        exportTimeout = higher.exportTimeout ?: exportTimeout,
        maxQueueSize = higher.maxQueueSize ?: maxQueueSize,
        maxExportBatchSize = higher.maxExportBatchSize ?: maxExportBatchSize,
        exporter = mergeNode(exporter, higher.exporter)
    )
}
