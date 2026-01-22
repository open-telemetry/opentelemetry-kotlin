package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.tracing.data.SpanData

/**
 * An interface for exporting spans to an arbitrary destination.
 */
@ExperimentalApi
public interface SpanExporter : TelemetryCloseable {

    /**
     * Exports a batch of spans. This operation is considered successful if the implementation
     * returns [OperationResultCode.Success]. If the export operation fails the batch must be dropped.
     */
    public suspend fun export(telemetry: List<SpanData>): OperationResultCode
}
