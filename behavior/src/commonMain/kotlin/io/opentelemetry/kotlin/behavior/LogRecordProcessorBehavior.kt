package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Processor used by the logger provider.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordprocessor
 */
@OptIn(ExperimentalApi::class)
data class LogRecordProcessorBehavior(
    /**
     * Simple processor, which exports each log record as it is emitted.
     */
    val simple: SimpleLogRecordProcessorBehavior? = null,
    /**
     * Batching settings for the configured log exporter.
     */
    val batch: BatchLogRecordProcessorBehavior? = null,
) : Behavior<LogRecordProcessorBehavior> {
    override fun mergeWith(higher: LogRecordProcessorBehavior): LogRecordProcessorBehavior = copy(
        simple = mergeNode(simple, higher.simple),
        batch = mergeNode(batch, higher.batch),
    )
}
