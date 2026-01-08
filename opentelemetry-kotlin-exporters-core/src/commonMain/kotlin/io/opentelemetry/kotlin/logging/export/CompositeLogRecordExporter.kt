package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperation
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@OptIn(ExperimentalApi::class)
internal class CompositeLogRecordExporter(
    private val exporters: List<LogRecordExporter>,
    private val sdkErrorHandler: SdkErrorHandler,
    private val telemetryCloseable: CompositeTelemetryCloseable = CompositeTelemetryCloseable(
        exporters,
        sdkErrorHandler,
    )
) : LogRecordExporter, TelemetryCloseable by telemetryCloseable {

    override fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        return batchExportOperation(
            exporters,
            sdkErrorHandler
        ) {
            it.export(telemetry)
        }
    }
}
