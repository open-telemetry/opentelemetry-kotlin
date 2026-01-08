package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperation
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

@OptIn(ExperimentalApi::class)
internal class CompositeLogRecordProcessor(
    private val processors: List<LogRecordProcessor>,
    private val sdkErrorHandler: SdkErrorHandler,
    private val telemetryCloseable: TelemetryCloseable = CompositeTelemetryCloseable(
        processors,
        sdkErrorHandler
    ),
) : LogRecordProcessor, TelemetryCloseable by telemetryCloseable {

    private val lock = ReentrantReadWriteLock()

    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        lock.write {
            batchExportOperation(
                processors,
                sdkErrorHandler
            ) {
                it.onEmit(log, context)
                OperationResultCode.Success
            }
        }
    }
}
