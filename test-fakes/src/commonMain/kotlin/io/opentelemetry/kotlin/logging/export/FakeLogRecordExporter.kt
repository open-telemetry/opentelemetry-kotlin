package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@OptIn(ExperimentalApi::class)
class FakeLogRecordExporter(
    var flushCode: () -> OperationResultCode = { OperationResultCode.Success },
    var shutdownCode: () -> OperationResultCode = { OperationResultCode.Success },
    var action: (telemetry: List<ReadableLogRecord>) -> OperationResultCode = { OperationResultCode.Success }
) : LogRecordExporter {

    val logs: MutableList<ReadableLogRecord> = mutableListOf()

    override fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        logs += telemetry
        return action(telemetry)
    }

    override fun forceFlush(): OperationResultCode = flushCode()
    override fun shutdown(): OperationResultCode = shutdownCode()
}
