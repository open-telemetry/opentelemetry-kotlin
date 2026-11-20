package io.opentelemetry.kotlin.behavior.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchProcessor
import io.opentelemetry.kotlin.behavior.BatchProcessorDelegate
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.mergeNode

/** Batching settings for a span processor, in milliseconds where applicable. */
@OptIn(ExperimentalApi::class)
data class BatchSpanProcessorBehavior(
    val delegate: BatchProcessorDelegate = BatchProcessorDelegate(),
) : Behavior<BatchSpanProcessorBehavior>, BatchProcessor by delegate {

    constructor(
        scheduleDelay: Long? = null,
        exportTimeout: Long? = null,
        maxQueueSize: Int? = null,
        maxExportBatchSize: Int? = null,
        exporter: SpanExporterBehavior? = null,
    ) : this(
        BatchProcessorDelegate(
            scheduleDelay,
            exportTimeout,
            maxQueueSize,
            maxExportBatchSize,
            exporter
        )
    )

    override fun mergeWith(higher: BatchSpanProcessorBehavior): BatchSpanProcessorBehavior = copy(
        delegate = BatchProcessorDelegate(
            scheduleDelay = higher.scheduleDelay ?: scheduleDelay,
            exportTimeout = higher.exportTimeout ?: exportTimeout,
            maxQueueSize = higher.maxQueueSize ?: maxQueueSize,
            maxExportBatchSize = higher.maxExportBatchSize ?: maxExportBatchSize,
            exporter = mergeNode(exporter as SpanExporterBehavior?, higher.exporter as SpanExporterBehavior?)
        ),
    )
}
