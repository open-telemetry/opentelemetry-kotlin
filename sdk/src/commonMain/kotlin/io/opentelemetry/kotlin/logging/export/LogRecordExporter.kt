package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

/**
 * An interface for exporting logs to an arbitrary destination.
 */
@ExperimentalApi
public interface LogRecordExporter : TelemetryCloseable {

    /**
     * Exports a batch of logs. This operation is considered successful if the implementation
     * returns [OperationResultCode.Success]. If the export operation fails the batch must be dropped.
     */
    public suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode
}
