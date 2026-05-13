package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

internal object NoopLogRecordExporter : LogRecordExporter {
    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode = OperationResultCode.Failure
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
