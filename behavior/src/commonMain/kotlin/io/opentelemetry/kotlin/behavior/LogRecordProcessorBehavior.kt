package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Processor used by the logger provider.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordprocessor
 */
@ExperimentalApi
data class LogRecordProcessorBehavior(
    /**
     * Console log exporter.
     */
    val console: ConsoleExporterBehavior? = null,
    /**
     * HTTP log exporter.
     */
    val http: OtlpHttpExporterBehavior? = null,

    /**
     * Simple processor, which exports each log record as it is emitted.
     */
    val simple: SimpleLogRecordProcessorBehavior? = null,

    /** Batching settings for the configured log exporter. */
    val batch: BatchLogRecordProcessorBehavior? = null,

) : Behavior<LogRecordProcessorBehavior> {

    override fun mergeWith(higher: LogRecordProcessorBehavior): LogRecordProcessorBehavior = copy(
        console = mergeNode(console, higher.console),
        http = mergeNode(http, higher.http),
        simple = mergeNode(simple, higher.simple),
        batch = mergeNode(batch, higher.batch),
    )
}
