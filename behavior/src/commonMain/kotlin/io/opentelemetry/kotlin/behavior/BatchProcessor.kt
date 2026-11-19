package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
interface BatchProcessor {
    /** Delay between consecutive exports, in milliseconds. Must be non-negative. */
    val scheduleDelay: Long?

    /** Maximum export duration, in milliseconds. Zero means no limit. Must be non-negative. */
    val exportTimeout: Long?

    /** Requested maximum number of queued signals. May be adjusted to satisfy processor constraints. */
    val requestedQueueSize: Int?

    /** Maximum number of queued signals. Must be positive. */
    val maxQueueSize: Int?

    /** Maximum number of signals per export. Must be positive and no greater than [maxQueueSize]. */
    val maxExportBatchSize: Int?

    /** Signal exporter. */
    val exporter: Exporter?
}
