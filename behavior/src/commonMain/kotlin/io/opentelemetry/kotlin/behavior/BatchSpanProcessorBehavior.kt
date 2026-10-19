package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/** Batching settings for a span processor, in milliseconds where applicable. */
@ExperimentalApi
data class BatchSpanProcessorBehavior(
    /** Delay between consecutive exports, in milliseconds. Must be non-negative. */
    val scheduleDelay: Long? = null,
    /** Maximum export duration, in milliseconds. Zero means no limit. Must be non-negative. */
    val exportTimeout: Long? = null,
    /** Maximum number of queued spans. Must be positive. */
    val maxQueueSize: Int? = null,
    /** Maximum spans per export. Must be positive and no greater than [maxQueueSize]. */
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

    companion object {
        const val DEFAULT_SCHEDULE_DELAY = 5_000L
        const val DEFAULT_EXPORT_TIMEOUT = 30_000L
        const val DEFAULT_MAX_QUEUE_SIZE = 2_048
        const val DEFAULT_MAX_EXPORT_BATCH_SIZE = 512
    }
}
