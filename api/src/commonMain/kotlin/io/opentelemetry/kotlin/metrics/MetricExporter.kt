package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.metrics.data.MetricData
import io.opentelemetry.kotlin.metrics.export.ExportResult

/**
 * A metric exporter that exports metrics to a backend system.
 *
 * Implementations of this interface are typically registered with a MeterProvider and are invoked
 * periodically to export the collected metrics.
 *
 * @see io.opentelemetry.kotlin.tracing.SpanExporter
 * @see io.opentelemetry.kotlin.logging.LogRecordExporter
 */
@ExperimentalApi
interface MetricExporter {
    /**
     * Exports a batch of metrics to the backend system.
     *
     * @param metrics The metrics to export.
     * @return The result of the export operation.
     */
    fun export(metrics: List<MetricData>): ExportResult

    /**
     * Called when the exporter is no longer needed and should release any resources.
     *
     * @return The result of the shutdown operation.
     */
    fun shutdown(): ExportResult
}
