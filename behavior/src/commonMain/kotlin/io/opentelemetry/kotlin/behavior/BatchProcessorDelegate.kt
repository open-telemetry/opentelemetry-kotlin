package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
data class BatchProcessorDelegate<T : Exporter>(
    override val scheduleDelay: Long? = null,
    override val exportTimeout: Long? = null,
    override val maxQueueSize: Int? = null,
    override val maxExportBatchSize: Int? = null,
    override val exporter: T? = null,
) : BatchProcessor
