package io.opentelemetry.kotlin.behavior.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BatchProcessor
import io.opentelemetry.kotlin.behavior.BatchProcessorDelegate
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Batching settings for the logger provider's processor. The exporter is configured on
 * [LogRecordProcessorBehavior]. Each null field is unset and defers to a lower configuration
 * layer or the SDK default.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor
 */
@OptIn(ExperimentalApi::class)
data class BatchLogRecordProcessorBehavior(
    val delegate: BatchProcessorDelegate = BatchProcessorDelegate(),
) : Behavior<BatchLogRecordProcessorBehavior>, BatchProcessor by delegate {

    constructor(
        scheduleDelay: Long? = null,
        exportTimeout: Long? = null,
        maxQueueSize: Int? = null,
        maxExportBatchSize: Int? = null,
        exporter: LogRecordExporterBehavior? = null,
    ) : this(
        BatchProcessorDelegate(
            scheduleDelay,
            exportTimeout,
            maxQueueSize,
            maxExportBatchSize,
            exporter
        )
    )

    override fun mergeWith(higher: BatchLogRecordProcessorBehavior): BatchLogRecordProcessorBehavior = copy(
        delegate = BatchProcessorDelegate(
            scheduleDelay = higher.scheduleDelay ?: scheduleDelay,
            exportTimeout = higher.exportTimeout ?: exportTimeout,
            maxQueueSize = higher.maxQueueSize ?: maxQueueSize,
            maxExportBatchSize = higher.maxExportBatchSize ?: maxExportBatchSize,
            exporter = mergeNode(
                exporter as LogRecordExporterBehavior?,
                higher.exporter as LogRecordExporterBehavior?
            )
        ),
    )
}
