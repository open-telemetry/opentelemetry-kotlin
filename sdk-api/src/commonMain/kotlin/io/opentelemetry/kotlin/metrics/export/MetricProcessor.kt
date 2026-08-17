package io.opentelemetry.kotlin.metrics.export

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.TelemetryCloseable

/**
 * Processes metrics before they are exported as batches.
 */
public interface MetricProcessor : TelemetryCloseable {

    /**
     * Invoked when a metric record is emitted.
     *
     * @param metric The metric record that has been emitted.
     * @param context The context associated with the metric record.
     */
    public fun onEmit(metric: LongMetricRecord, context: Context)

    /**
     * Returns whether a metric should be emitted based on the provided parameters.
     *
     * This method allows processors to indicate via a boolean return value whether they would
     * filter out a metric before it is created, which helps avoid the cost of creating
     * unnecessary metrics.
     *
     * @param context The context associated with the metric.
     * @param name The name of the metric
     * @return true if a metric should be emitted
     */
    public fun enabled(
        context: Context,
        name: String,
    ): Boolean = true
}
