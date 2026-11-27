package io.opentelemetry.kotlin.metrics.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.metrics.data.MetricData

/**
 * An interface for exporting metrics to an arbitrary destination.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metric-exporter
 */
@ExperimentalApi
public interface MetricExporter : TelemetryCloseable {

    /**
     * Exports a batch of metrics. This operation is considered successful if the implementation
     * returns [OperationResultCode.Success]. If the export operation fails the batch must be dropped.
     */
    public suspend fun export(telemetry: List<MetricData>): OperationResultCode
}
