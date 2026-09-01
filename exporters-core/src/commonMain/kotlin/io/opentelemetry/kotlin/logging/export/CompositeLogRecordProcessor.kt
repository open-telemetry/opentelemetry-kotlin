package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperation
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

internal class CompositeLogRecordProcessor(
    private val processors: List<LogRecordProcessor>,
    private val sdkErrorHandler: SdkErrorHandler,
    private val telemetryCloseable: TelemetryCloseable = CompositeTelemetryCloseable(
        processors,
        sdkErrorHandler
    ),
) : LogRecordProcessor, TelemetryCloseable by telemetryCloseable {

    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        batchExportOperation(
            processors,
            sdkErrorHandler
        ) {
            it.onEmit(log, context)
            OperationResultCode.Success
        }
    }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean {
        // returns true if _any_ of the processors are enabled.
        return processors.any { it.enabled(context, instrumentationScopeInfo, severityNumber, eventName) }
    }
}
